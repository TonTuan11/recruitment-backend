package com.tihuz.user_service.client.fallback;

import com.tihuz.common.dto.ApiResponse;
import com.tihuz.user_service.client.CompanyClient;
import com.tihuz.user_service.dto.request.CompanyRequest;
import com.tihuz.user_service.dto.response.CompanyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CompanyClientFallback implements CompanyClient {



    @Override
    public ApiResponse<CompanyResponse> createCompany(String token, CompanyRequest request) {
        log.error("Fallback: Company service is down or error when creating company: {}", request.getName());

        // Trả về response mặc định báo lỗi
        return ApiResponse.<CompanyResponse>builder()
                .code(5001)
                .message("Company service is unavailable. Please try again later.")
                .build();
    }

    @Override
    public ApiResponse<CompanyResponse> getCompanyNameById(String token, Long companyId) {
        log.warn("Fallback COMPANY SERVICE called for companyId: {}", companyId);

        CompanyResponse companyResponse = CompanyResponse.builder()
                .id(companyId)
                .name("Lỗi mẹ rồi")
                .build();

        return ApiResponse.<CompanyResponse>builder()
                .result(companyResponse)
                .build();
    }
}