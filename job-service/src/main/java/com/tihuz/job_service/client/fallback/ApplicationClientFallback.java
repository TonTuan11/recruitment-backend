package com.tihuz.job_service.client.fallback;

import com.tihuz.common.dto.ApiResponse;
import com.tihuz.job_service.client.ApplicationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationClientFallback implements ApplicationClient {


    @Override
    public ApiResponse<Long> countPendingApplicationsByJob(Long jobId, Long userId, String token) {
        System.out.println("=== FALLBACK CALLED! jobId: " + jobId + ", userId: " + userId);
        System.out.println("=== Token in fallback: " + token);
        return ApiResponse.<Long>builder()
                .result(1L)
                .build();
    }

    @Override
    public ApiResponse<Void> deleteApplicationsByJobId(Long jobId)
    {
        log.error("Fallback: Cannot delete applications for job {}", jobId);
        return ApiResponse.<Void>builder()
                .code(404)
                .message("Failed to delete applications")
                .build();
    }
}