package com.tihuz.user_service.controller;

import com.tihuz.common.dto.ApiResponse;
import com.tihuz.user_service.client.CompanyClient;
import com.tihuz.user_service.dto.request.CompanyRequest;
import com.tihuz.user_service.dto.request.UserUpdateRequest;
import com.tihuz.user_service.dto.response.CompanyResponse;
import com.tihuz.user_service.dto.response.UserResponse;
import com.tihuz.user_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.MDC;

@RequestMapping("/users")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor

public class UserController {
    UserService userService;
    CompanyClient companyClient;
    HttpServletRequest httpServletRequest;


    @GetMapping("/test-company")
    public ApiResponse<CompanyResponse> testCreateCompany()
    {


        try {
            String token = httpServletRequest.getHeader("Authorization");
            ApiResponse<CompanyResponse> response = companyClient.getCompanyNameById(token, 1L);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.<CompanyResponse>builder()
                    .code(500)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }



    @GetMapping("/test")
    public String test() {
        System.out.println("TraceId = " + MDC.get("traceId"));
        return "ok";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<List<UserResponse>> getAll()
    {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAll())
                .build();

    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active")
    public ApiResponse<Page<UserResponse>> getActiveUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword)
    {
        Pageable pageable = PageRequest.of(page, size);

        return ApiResponse.<Page<UserResponse>>builder()
                .result(userService.getActiveUsers(pageable, keyword))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/deleted")
    public ApiResponse<Page<UserResponse>> getDeletedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size);

        return ApiResponse.<Page<UserResponse>>builder()
                .result(userService.getDeletedUsers(pageable, keyword))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/restore/{userId}")
    public ApiResponse<Void> restoreUser(@PathVariable Long userId)
    {

        userService.restoreUser(userId);
        return ApiResponse.<Void>builder()
                .message("Khôi phục user thành công")
                .build();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('COMPANY') or #userId.toString() == authentication.name")
    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUserId(@PathVariable Long userId)
    {

        return ApiResponse.<UserResponse>builder()
                .result( userService.getUserId(userId))
                .build();

    }


    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyInfo()
    {
        return ApiResponse.<UserResponse>builder()
                .result( userService.getMyInfo())
                .build();

    }

    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    @PutMapping("/{userId}")
    public ApiResponse<UserResponse>  updateUser(@PathVariable Long userId, @RequestBody @Valid UserUpdateRequest request)
    {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId,request))
                .build();

    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/soft/{userId}")
    public ApiResponse<Void> softDeleteCompany(@PathVariable Long userId) {
        userService.softDeleteUserId(userId);
        return ApiResponse.<Void>builder()
                .message("Xóa user thành công")
                .build();
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/hard/{userId}")
    public ApiResponse<Void> hardDeleteCompany(@PathVariable Long userId) {
        userService.hardDeleteUserId(userId);
        return ApiResponse.<Void>builder()
                .message("Xóa user thành công")
                .build();
    }
}
