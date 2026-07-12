package com.tihuz.user_service.controller;


import com.nimbusds.jose.JOSEException;
import com.tihuz.common.dto.ApiResponse;
import com.tihuz.user_service.dto.request.*;
import com.tihuz.user_service.dto.response.AuthenticationResponse;
import com.tihuz.user_service.dto.response.UserResponse;
import com.tihuz.user_service.service.AuthenticationService;
import com.tihuz.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthenticationController
{

    AuthenticationService authenticationService;
    UserService userService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody @Valid AuthenticationRequest request) throws JOSEException
    {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.login(request))
                .build();
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody @Valid UserCreatedRequest request)
    {
        return ApiResponse.<UserResponse>builder()
                .result(  authenticationService.register(request))
                .build();
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthenticationResponse> refreshToken(@RequestBody @Valid RefreshRequest request) throws ParseException, JOSEException
    {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.refresh(request.getAccessToken(),request.getRefreshToken()))
                .build();

    }

    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@RequestBody @Valid ForgetPasswordRequest request)
    {
        userService.requestPasswordReset(request.getEmail());
        return ApiResponse.<String>builder()
                .message("OTP has been sent to your email")
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request)
    {
        userService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ApiResponse.<String>builder()
                .message("Password reset successfully")
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {

        authenticationService.logout(request.getAccessToken(),request.getRefreshToken());

        return ApiResponse.<Void>builder()
                .message("Logout successfully")
                .build();
    }

}
