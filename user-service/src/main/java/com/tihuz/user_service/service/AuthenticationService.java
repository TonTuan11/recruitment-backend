package com.tihuz.user_service.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.tihuz.common.dto.ApiResponse;
import com.tihuz.common.event.UserEvent;
import com.tihuz.common.exception.AppException;
import com.tihuz.common.exception.ErrorCode;
import com.tihuz.common.redis.BlacklistTokenService;
import com.tihuz.user_service.Enum.RoleType;
import com.tihuz.user_service.client.CompanyClient;
import com.tihuz.user_service.dto.request.AuthenticationRequest;
import com.tihuz.user_service.dto.request.CompanyRequest;
import com.tihuz.user_service.dto.request.UserCreatedRequest;
import com.tihuz.user_service.dto.response.AuthenticationResponse;
import com.tihuz.user_service.dto.response.CompanyResponse;
import com.tihuz.user_service.dto.response.UserResponse;
import com.tihuz.user_service.entity.User;
import com.tihuz.user_service.mapper.UserMapper;
import com.tihuz.user_service.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService
{

    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    UserMapper userMapper;
    KafkaTemplate<String, Object> kafkaTemplate;
    CompanyClient companyClient;

    RefreshTokenService refreshTokenService;
    BlacklistTokenService blacklistTokenService;


    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESH_DURATION;


    public AuthenticationResponse login(AuthenticationRequest request) throws JOSEException
    {
        User user=userRepository.findByUsernameAndIsDeletedFalse(request.getUsername())
                .orElseThrow(()-> new AppException(ErrorCode.USER_INVALID));

        if(!passwordEncoder.matches(request.getPassword(),user.getPassword() ))
        {
            throw new AppException(ErrorCode.PASSWORD_INVALID);
        }

        String accessToken = generateToken(user);

        String refreshToken = generateRefreshToken(user);

        // save redis
        refreshTokenService.save(user.getId(), refreshToken, REFRESH_DURATION);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }



//    FE chỉ gọi 1 API: POST /auth/register
//  ↓
//    BE (User Service):
//        1. Parse request, thấy role = COMPANY
//  2. Tạo company (gọi Feign hoặc event)
//  3. Nếu thành công → tạo user với companyId
//  4. Nếu thất bại → rollback, không tạo user
//  5. Trả về response

//    public UserResponse register (UserCreatedRequest request) {
//
//
//        if (userRepository.existsByUsername(request.getUsername()))
//        {
//            throw new AppException(ErrorCode.USER_EXITS);
//        }
//
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
//        }
//
//        User user = userMapper.toUser(request);
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//
//
//        if (request.isCompanyRegister())
//        {
//            user.setRole(RoleType.COMPANY);
//            user.setCompanyId(request.getCompanyId() != null ? Long.valueOf(request.getCompanyId()) : null);
//        }
//        else
//        {
//            user.setRole(RoleType.USER);
//            user.setCompanyId(null);
//        }
//
//        return userMapper.toUserResponse(userRepository.save(user));
//    }


    @Transactional
    public UserResponse register(UserCreatedRequest request)
    {
        // Check username/email exist
        if (userRepository.existsByUsername(request.getUsername()))
        {
            throw new AppException(ErrorCode.USER_EXITS);
        }
        if (userRepository.existsByEmail(request.getEmail()))
        {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Long companyId = null;

        // if register is company →  create the company first
        if (request.isCompanyRegister())
        {
            try
            {
                // Call company-service to create company
                CompanyRequest companyRequest = CompanyRequest.builder()
                        .name(request.getCompanyName())
                        .address(request.getCompanyAddress())
                        .category(request.getCompanyCategory())
                        .email(request.getEmail())
                        .build();

//                String token = httpServletRequest.getHeader("Authorization");
                ApiResponse<CompanyResponse> response = companyClient.createCompany(companyRequest);

                // Check response from fallback or error
                if (response == null || response.getResult() == null || response.getCode() != 200)
                {
                    log.error("Call COMPANY-SERVICE error: {}", response != null ? response.getMessage() : "Unknown error");
                    throw new AppException(ErrorCode.COMPANY_CREATE_FAILED);
                }

                companyId = response.getResult().getId();

            }
            catch (Exception e)
            {
                log.error("Failed to create company: {}", e.getMessage());
                throw new AppException(ErrorCode.COMPANY_CREATE_FAILED);
            }
        }

        // Create user
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.isCompanyRegister() ? RoleType.COMPANY : RoleType.USER);
        user.setCompanyId(companyId);
        user.setIsDeleted(false);

        // Save user
        User saved = userRepository.save(user);

        // Send event if registering as a company
//        if (request.isCompanyRegister())
//        {
            UserEvent event = new UserEvent();
            event.setUserId(saved.getId());
            event.setEmail(saved.getEmail());
            event.setUsername(saved.getUsername());
            event.setCompanyId(companyId);
            event.setAction("CREATED");
            kafkaTemplate.send(UserEvent.TOPIC, event);


        return userMapper.toUserResponse(saved);
    }



    private String generateToken(User user) throws JOSEException
    {
         JWSHeader jwsHeader= new JWSHeader(JWSAlgorithm.HS512);

         JWTClaimsSet jwtClaimsSet=new JWTClaimsSet.Builder()
                 .subject(String.valueOf(user.getId()))
                 .issueTime(new Date())
                 .expirationTime(Date.from(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS)))
                 .jwtID(UUID.randomUUID().toString())
                 .claim("scope","ROLE_"+user.getRole())
                 .build();

         Payload payload= new Payload(jwtClaimsSet.toJSONObject());

         JWSObject jwsObject=new JWSObject(jwsHeader,payload);

         jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
         return jwsObject.serialize();

     }


     private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException
     {
         // check blacklist
         if (blacklistTokenService.exists(token))
         {
             throw new AppException(ErrorCode.UNAUTHORIZED_EXCEPTION);
         }

        // create verifier token by JWSVerifier
        JWSVerifier verifier=new MACVerifier(SIGNER_KEY.getBytes());

        // Parse the token string -> SignedJWT object
        SignedJWT signedJWT=SignedJWT.parse(token);
        // check sign
         boolean verified = signedJWT.verify(verifier);

        // check expirationTime
         Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

         if (!verified || expirationTime.before(new Date()))
         {
             throw new AppException(ErrorCode.UNAUTHORIZED_EXCEPTION);
         }

         // if is verify Refresh Token
         if (isRefresh)
         {
             String type = signedJWT.getJWTClaimsSet().getStringClaim("type");

             if (!"refresh".equals(type))
             {
                 throw new AppException(ErrorCode.UNAUTHORIZED_EXCEPTION);
             }
         }
         return signedJWT;
     }

    public AuthenticationResponse refresh(String accessToken, String refreshToken) throws ParseException, JOSEException
    {

        // Verify refresh token
        SignedJWT refreshJwt  = verifyToken(refreshToken, true);

        // Get userId from JWT
        Long userId = Long.valueOf(refreshJwt .getJWTClaimsSet().getSubject());

        // get refresh token in Redis
        String redisToken = refreshTokenService.get(userId);

        if (redisToken == null || !redisToken.equals(refreshToken))
        {
            throw new AppException(ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        // Verify old access token
        SignedJWT accessJwt = verifyToken(accessToken, false);

        // Blacklist old access token
        Date expiration = accessJwt.getJWTClaimsSet().getExpirationTime();

        long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000;

        if (ttl > 0)
        {
            blacklistTokenService.save(accessToken, ttl);
        }


        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_INVALID));

        // generate new token
        String newAccessToken = generateToken(user);
        String newRefreshToken = generateRefreshToken(user);

        // Overwrite the refresh token in Redis
        refreshTokenService.save(userId, newRefreshToken, REFRESH_DURATION);

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }



    private String generateRefreshToken(User user) throws JOSEException
    {

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getId()))
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(REFRESH_DURATION, ChronoUnit.SECONDS)))
                .jwtID(UUID.randomUUID().toString())
                .claim("type","refresh")
                .build();

        Payload payload= new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject=new JWSObject(header,payload);

        jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));

        return jwsObject.serialize();
    }


    public void logout(String accessToken, String refreshToken) throws ParseException, JOSEException
    {
        // verify refresh token
        SignedJWT refreshJwt = verifyToken(refreshToken, true);

        Long userId = Long.valueOf(refreshJwt.getJWTClaimsSet().getSubject());

        // xóa refresh token
        refreshTokenService.delete(userId);

        // verify access token
        SignedJWT accessJwt = verifyToken(accessToken, false);

        Date expiration = accessJwt.getJWTClaimsSet().getExpirationTime();

        long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000;

        if (ttl > 0)
        {
            blacklistTokenService.save(accessToken, ttl);
        }
    }

}
