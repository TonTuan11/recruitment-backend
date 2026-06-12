package com.tihuz.application_service.client.fallback;

import com.tihuz.application_service.client.JobClient;
import com.tihuz.application_service.dto.response.JobResponse;
import com.tihuz.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Slf4j
public class JobClientFallback implements JobClient {

    @Override
    public ApiResponse<Page<JobResponse>> getJobsByCompanyId(Long id, int page, int size) {
        log.error("FALLBACK JOB SERVICE: Cannot get jobs for userId={}", id);
        return ApiResponse.<Page<JobResponse>>builder()
                .code(503)
                .result(new PageImpl<>(Collections.emptyList()))
                .message("Job service unavailable")
                .build();
    }

    @Override
    public ApiResponse<JobResponse> getJobById(Long id) {
        log.error("FALLBACK JOB SERVICE: Cannot get job by id={}", id);
        return ApiResponse.<JobResponse>builder()
                .code(503)
                .result(null)
                .message("Job service unavailable")
                .build();
    }
}