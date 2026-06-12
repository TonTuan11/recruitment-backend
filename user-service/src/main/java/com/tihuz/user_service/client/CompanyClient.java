package com.tihuz.user_service.client;

import com.tihuz.common.dto.ApiResponse;
import com.tihuz.user_service.client.fallback.CompanyClientFallback;
import com.tihuz.user_service.dto.request.CompanyRequest;
import com.tihuz.user_service.dto.response.CompanyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "COMPANY-SERVICE",
        fallback = CompanyClientFallback.class
)
public interface CompanyClient
{
    @PostMapping("/companies")
    ApiResponse<CompanyResponse> createCompany(
            @RequestHeader("Authorization") String token,
            @RequestBody CompanyRequest request);

    @GetMapping("/companies/{id}")
    ApiResponse<CompanyResponse> getCompanyNameById(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long companyId
    );
}