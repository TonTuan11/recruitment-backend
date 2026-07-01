package com.tihuz.user_service.service;

import com.tihuz.common.exception.AppException;
import com.tihuz.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String OTP_PREFIX = "otp:";
    private static final long OTP_EXPIRATION_MINUTES = 4;

    // Save OTP with a 4-minute TTL
    public void saveOtp(String email, String otp)
    {
        String key = OTP_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, OTP_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        log.info(" OTP saved for {} with TTL {} minutes", email, OTP_EXPIRATION_MINUTES);
    }

    // Get OTP from Redis
    public String getOtp(String email)
    {
        String key = OTP_PREFIX + email;
        return redisTemplate.opsForValue().get(key);
    }

    // Delete OTP
    public void deleteOtp(String email)
    {
        String key = OTP_PREFIX + email;
        redisTemplate.delete(key);
        log.info("OTP deleted for {}", email);
    }

    //  Verify OTP
    public boolean verifyOtp(String email, String otp)
    {
        String storedOtp = getOtp(email);
        if (storedOtp == null)
        {
            log.warn(" No OTP found or expired for {}", email);
            throw new AppException(ErrorCode.OTP_NULL);
        }

        boolean isValid = storedOtp.equals(otp);

        if (isValid)
        {
            deleteOtp(email); // OTP can only be used once
            log.info("OTP verified successfully for {}", email);
        }
        else
        {
            log.warn(" Invalid OTP for {}", email);
            throw new AppException(ErrorCode.INVALID_OTP);
        }
        return isValid;
    }
}