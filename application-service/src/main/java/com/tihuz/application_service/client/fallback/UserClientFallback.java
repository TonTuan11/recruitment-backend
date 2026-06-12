package com.tihuz.application_service.client.fallback;

import com.tihuz.common.dto.ApiResponse;
import com.tihuz.application_service.client.UserClient;
import com.tihuz.application_service.dto.response.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
@Component
@Slf4j
public class UserClientFallback implements UserClient {

    @Override
    public ApiResponse<UserResponse> getUserById(Long userId) {
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