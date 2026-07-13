package com.tihuz.application_service.client;

import com.tihuz.common.dto.ApiResponse;
import com.tihuz.application_service.client.fallback.UserClientFallback;
import com.tihuz.application_service.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "USER-SERVICE",
        fallback = UserClientFallback.class
)
public interface UserClient
{

    @GetMapping("/users/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable("id") Long userId);
}
