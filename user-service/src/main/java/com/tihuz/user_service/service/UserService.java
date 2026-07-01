package com.tihuz.user_service.service;

import com.tihuz.common.event.CompanyEvent;
import com.tihuz.common.event.PasswordResetEvent;
import com.tihuz.common.event.UserEvent;
import com.tihuz.common.exception.AppException;
import com.tihuz.common.exception.ErrorCode;
import com.tihuz.user_service.Enum.RoleType;
import com.tihuz.user_service.dto.request.UserUpdateRequest;
import com.tihuz.user_service.dto.response.UserResponse;
import com.tihuz.user_service.entity.User;
import com.tihuz.user_service.mapper.UserMapper;
import com.tihuz.user_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserService
{

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    KafkaTemplate<String, Object> kafkaTemplate;
    OtpService otpService;

    public UserResponse updateUser(Long UserId, UserUpdateRequest request)
    {
        User user=userRepository.findByIdAndIsDeletedFalse(UserId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXITS));

        Optional.ofNullable(request.getUsername()).ifPresent(user::setUsername);
        Optional.ofNullable(request.getEmail()).ifPresent(user::setEmail);
        Optional.ofNullable(request.getAvatarUrl()).ifPresent(user::setAvatarUrl);
        Optional.ofNullable(request.getPassword())
                .ifPresent(pw -> user.setPassword(passwordEncoder.encode(pw)));


        UserEvent event = new UserEvent();
        event.setUserId(user.getId());
        event.setCompanyId(user.getCompanyId());
        event.setEmail(user.getEmail());
        event.setAction("UPDATE");
        kafkaTemplate.send(UserEvent.TOPIC, event);

        return userMapper.toUserResponse(userRepository.save(user));
    }



    // Request send OTP
    @Transactional
    public void requestPasswordReset(String email)
    {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));

        //String otp = String.format("%06d", new Random().nextInt(999999));
        String otp = String.valueOf(new Random().nextInt(900000)+100000);
        log.info("Generated OTP = {}", otp);

        // Save OTP to Redis instead of the local cache
        otpService.saveOtp(email, otp);

        // Publish event
        PasswordResetEvent event = new PasswordResetEvent();
        event.setUserId(user.getId());
        event.setEmail(email);
        event.setUsername(user.getUsername());
        event.setOtp(otp);
        log.info("OTP in event before Kafka = {}", event.getOtp());
        kafkaTemplate.send(PasswordResetEvent.TOPIC, event);
        log.info(" Published PasswordResetEvent for user: {}", email);
    }

    // Reset password with OTP
    @Transactional
    public void resetPassword(String email, String otp, String newPassword)
    {
        // check otp in Redis
        if (!otpService.verifyOtp(email, otp))
        {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        // find user
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info(" Password reset successfully for user: {}", email);
    }


    public List<UserResponse> getAll()
    {
        return userRepository.findAllByIsDeletedFalse()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
    }



    public Page<UserResponse> getActiveUsers(Pageable pageable, String keyword)
    {
        Page<User> page;
        if (keyword != null && !keyword.isEmpty()) {
            page = userRepository.findByIsDeletedFalseAndUsernameContainingIgnoreCaseOrIsDeletedFalseAndEmailContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            page = userRepository.findByIsDeletedFalse(pageable);
        }
        return page.map(userMapper::toUserResponse);
    }

    public Page<UserResponse> getDeletedUsers(Pageable pageable, String keyword)
    {
        Page<User> page;
        if (keyword != null && !keyword.isEmpty())
        {
            page = userRepository.findByIsDeletedTrueAndUsernameContainingIgnoreCaseOrIsDeletedTrueAndEmailContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            page = userRepository.findByIsDeletedTrue(pageable);
        }
        return page.map(userMapper::toUserResponse);
    }

    public void restoreUser(Long userId)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));
        user.setIsDeleted(false);
        userRepository.save(user);

        // Send event user restore
        UserEvent event = new UserEvent();
        event.setUserId(user.getId());
        event.setCompanyId(user.getCompanyId());
        event.setAction("RESTORE");
        kafkaTemplate.send(UserEvent.TOPIC, event);
    }




    public UserResponse getUserId(Long UserId)
    {
        var user=userRepository.findByIdAndIsDeletedFalse(UserId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXITS));

//        try {
//            Thread.sleep(6000); // sleep 6 giây
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return userMapper.toUserResponse(user);


    }

    public UserResponse getMyInfo()
    {
        var context= SecurityContextHolder.getContext().getAuthentication();

        User user=userRepository.findByIdAndIsDeletedFalse(Long.valueOf(context.getName()))
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXITS));


        return userMapper.toUserResponse(user);
    }


    public String softDeleteUserId(Long userId)
    {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));

        user.setIsDeleted(true);
        userRepository.save(user);

        // Send event user deleted
        UserEvent event = new UserEvent();
        event.setUserId(user.getId());
        event.setCompanyId(user.getCompanyId());
        event.setAction("SOFT_DELETED");
        kafkaTemplate.send(UserEvent.TOPIC, event);

        return "User soft deleted";
    }


    public String hardDeleteUserId(Long userId)
    {
        User user = userRepository.findByIdAndIsDeletedTrue(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));

        userRepository.deleteById(user.getId());

        // Send event user deleted
        UserEvent event = new UserEvent();
        event.setUserId(user.getId());
        event.setCompanyId(user.getCompanyId());
        event.setAction("HARD_DELETED");
        kafkaTemplate.send(UserEvent.TOPIC, event);

        return "User hard deleted";
    }




    @KafkaListener(topics = CompanyEvent.TOPIC, groupId = "user-service-group")
    public void handleCompanyEvent(CompanyEvent event)
    {
        log.info("User Service received CompanyEvent: Action={}, CompanyId={}",
                event.getAction(), event.getCompanyId());

        if (event.getCompanyId() == null)
        {
            log.warn("CompanyEvent missing companyId, skipping");
            return;
        }

        switch (event.getAction())
        {
            case "SOFT_DELETED":
                softDeleteUserByCompanyId(event.getCompanyId());
                break;

            case "HARD_DELETED":
                hardDeleteUserByCompanyId(event.getCompanyId());
                break;

            case "RESTORE":
                restoreUserByCompanyId(event.getCompanyId());
                break;

            default:
                log.debug("No action needed for company event: {}", event.getAction());
        }
    }

    private void softDeleteUserByCompanyId(Long companyId)
    {
        User user = userRepository.findByCompanyIdAndRoleAndIsDeletedFalse(companyId, RoleType.COMPANY)
                .orElse(null);
        if (user == null)
        {
            log.warn("No active COMPANY user found for companyId: {}", companyId);
            return;
        }
        user.setIsDeleted(true);
        userRepository.save(user);
        log.info("Soft deleted user {} for company {}", user.getId(), companyId);
    }

    private void hardDeleteUserByCompanyId(Long companyId)
    {
        User user = userRepository.findByCompanyIdAndRole(companyId, RoleType.COMPANY)
                .orElse(null);
        if (user == null)
        {
            log.warn("No COMPANY user found for companyId: {}", companyId);
            return;
        }
        userRepository.delete(user);
        log.info("Hard deleted user {} for company {}", user.getId(), companyId);
    }

    private void restoreUserByCompanyId(Long companyId)
    {
        List<User> users = userRepository.findAllByCompanyId(companyId);

        if (users.isEmpty()) return;

        users.forEach(u -> u.setIsDeleted(false));

        userRepository.saveAll(users);
    }


}
