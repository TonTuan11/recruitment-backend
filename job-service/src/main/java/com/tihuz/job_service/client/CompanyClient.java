package com.tihuz.job_service.client;

import com.tihuz.common.dto.ApiResponse;
import com.tihuz.job_service.client.fallback.CompanyClientFallback;

import com.tihuz.job_service.dto.response.CompanyResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;


@FeignClient(name = "COMPANY-SERVICE",
        fallback = CompanyClientFallback.class)
public interface CompanyClient {

    @GetMapping("/companies/{id}")
    ApiResponse<CompanyResponse> getCompanyNameById(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long companyId
    );
}
