package com.tihuz.job_service.client;

import com.tihuz.common.dto.ApiResponse;
import com.tihuz.job_service.client.fallback.UserClientFallback;
import com.tihuz.job_service.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;


@FeignClient(name = "USER-SERVICE",
fallback = UserClientFallback.class)
public interface UserClient {
    @GetMapping("/users/{id}")
    ApiResponse<UserResponse> getUserById(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long userId
    );

}
