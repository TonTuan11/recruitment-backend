package com.tihuz.application_service.client;

import com.tihuz.application_service.client.fallback.JobClientFallback;
import com.tihuz.application_service.dto.response.JobResponse;
import com.tihuz.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "JOB-SERVICE", fallback = JobClientFallback.class)
public interface JobClient
{

    // SỬA: Thêm ApiResponse wrapper
    @GetMapping("/jobs/user/{id}")
    ApiResponse<Page<JobResponse>> getJobsByCompanyId(
            @PathVariable("id") Long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size);

    @GetMapping("/jobs/{id}")
    ApiResponse<JobResponse> getJobById(@PathVariable("id") Long id);
}