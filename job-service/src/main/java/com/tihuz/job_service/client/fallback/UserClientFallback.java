package com.tihuz.job_service.client.fallback;

import com.tihuz.common.dto.ApiResponse;
import com.tihuz.job_service.client.UserClient;
import com.tihuz.job_service.dto.response.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserClientFallback implements UserClient {

    @Override
    public ApiResponse<UserResponse> getUserById(String token, Long userId) {
        log.warn("Fallback USER SERVICE userId={}", userId);
        UserResponse user = UserResponse.builder()
                .id(-1L)
                .username("unknown")
                .avatarUrl("unknown")
                .build();

        return ApiResponse.<UserResponse>builder()
                .result(user)
                .build();
    }
}