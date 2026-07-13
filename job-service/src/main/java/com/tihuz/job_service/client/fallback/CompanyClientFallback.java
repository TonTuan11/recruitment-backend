package com.tihuz.job_service.client.fallback;

import com.tihuz.common.dto.ApiResponse;
import com.tihuz.job_service.client.CompanyClient;
import com.tihuz.job_service.dto.response.CompanyResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CompanyClientFallback implements CompanyClient {
    @Override
    public ApiResponse<CompanyResponse> getCompanyNameById( Long companyId) {
        log.warn("Fallback COMPANY SERVICE called for companyId: {}", companyId);

        CompanyResponse companyResponse = CompanyResponse.builder()
                .id(companyId)
                .name("Unknown Company")
                .logo("")
                .build();

        return ApiResponse.<CompanyResponse>builder()
                .result(companyResponse)
                .build();
    }
}
