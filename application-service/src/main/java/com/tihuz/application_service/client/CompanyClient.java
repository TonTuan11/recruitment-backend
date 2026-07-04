package com.tihuz.application_service.client;

import com.tihuz.application_service.dto.response.CompanyResponse;
import com.tihuz.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "COMPANY-SERVICE", path = "/api/companies")
public interface CompanyClient {

    @GetMapping("/{id}")
    ApiResponse<CompanyResponse> getCompanyById(@PathVariable("id") Long companyId);
}