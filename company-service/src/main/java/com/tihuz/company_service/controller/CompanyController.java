package com.tihuz.company_service.controller;

import com.tihuz.common.dto.ApiResponse;
import com.tihuz.company_service.dto.CompanyRequest;
import com.tihuz.company_service.dto.CompanyResponse;
import com.tihuz.company_service.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ApiResponse<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest request) {
        return ApiResponse.<CompanyResponse>builder()
                .result(companyService.createCompany(request))
                .message("Tạo công ty thành công")
                .build();
    }




    @PutMapping("/{id}")
    public ApiResponse<CompanyResponse> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest request) {
        return ApiResponse.<CompanyResponse>builder()
                .result(companyService.updateCompany(id, request))
                .message("Cập nhật công ty thành công")
                .build();
    }



    @GetMapping("/search")
    public ApiResponse<List<CompanyResponse>> searchCompanies(@RequestParam String keyword) {
        return ApiResponse.<List<CompanyResponse>>builder()
                .result(companyService.searchCompanies(keyword))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CompanyResponse> getCompanyById(@PathVariable Long id)
    {
        return ApiResponse.<CompanyResponse>builder()
                .result(companyService.getCompanyById(id))
                .build();
    }

    @GetMapping("/name/{name}")
    public ApiResponse<CompanyResponse> getCompanyByName(@PathVariable String name) {
        return ApiResponse.<CompanyResponse>builder()
                .result(companyService.getCompanyByName(name))
                .build();
    }


    @GetMapping("/categories")
    public ApiResponse<List<String>> getAllCategories() {
        return ApiResponse.<List<String>>builder()
                .result(companyService.getAllCategories())
                .build();
    }


    @GetMapping
    public ApiResponse<Page<CompanyResponse>> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return ApiResponse.<Page<CompanyResponse>>builder()
                .result(companyService.getAllCompanies(pageable))
                .build();
    }

    @GetMapping("/top")
    public ApiResponse<List<CompanyResponse>> getTopCompanies(
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.<List<CompanyResponse>>builder()
                .result(companyService.getTopCompanies(limit))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active")
    public ApiResponse<Page<CompanyResponse>> getActiveCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword)
    {
        Pageable pageable = PageRequest.of(page, size);

        return ApiResponse.<Page<CompanyResponse>>builder()
                .result(companyService.getActiveCompanies(pageable, keyword))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/deleted")
    public ApiResponse<Page<CompanyResponse>> getDeletedCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword)
    {
        Pageable pageable = PageRequest.of(page, size);

        return ApiResponse.<Page<CompanyResponse>>builder()
                .result(companyService.getDeletedCompanies(pageable, keyword))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/restore/{id}")
    public ApiResponse<Void> restoreCompany(@PathVariable Long id)
    {
        companyService.restoreCompany(id);

        return ApiResponse.<Void>builder()
                .message("Khôi phục công ty thành công")
                .build();
    }


    @DeleteMapping("/soft/{id}")
    public ApiResponse<Void> softDeleteCompany(@PathVariable Long id)
    {
        companyService.softDeleteCompany(id);
        return ApiResponse.<Void>builder()
                .message("Xóa công ty thành công")
                .build();
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/hard/{id}")
    public ApiResponse<Void> hardDeleteCompany(@PathVariable Long id)
    {
        companyService.hardDeleteCompany(id);
        return ApiResponse.<Void>builder()
                .message("Xóa công ty thành công")
                .build();
    }
}