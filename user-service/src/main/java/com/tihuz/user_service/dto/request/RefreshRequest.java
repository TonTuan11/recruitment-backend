package com.tihuz.user_service.dto.request;

import lombok.Data;

@Data
public class RefreshRequest {

    private String accessToken;
    private String refreshToken;

}