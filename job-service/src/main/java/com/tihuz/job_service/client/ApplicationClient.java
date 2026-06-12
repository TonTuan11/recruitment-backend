package com.tihuz.job_service.client;

import com.tihuz.common.dto.ApiResponse;
import com.tihuz.job_service.client.fallback.ApplicationClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "APPLICATION-SERVICE", fallback = ApplicationClientFallback.class)
public interface ApplicationClient {

    @GetMapping("/applications/count-pending-by-job/{jobId}")
    ApiResponse<Long> countPendingApplicationsByJob(
            @PathVariable("jobId") Long jobId,
            @RequestParam("userId") Long userId,
            @RequestHeader("Authorization") String authorization);

    @DeleteMapping("/applications/by-job/{jobId}")
    ApiResponse<Void> deleteApplicationsByJobId(@PathVariable("jobId") Long jobId,
                                                @RequestHeader("Authorization") String token);


}