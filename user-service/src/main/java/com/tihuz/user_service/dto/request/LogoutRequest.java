package com.tihuz.user_service.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class LogoutRequest
{
     String accessToken;
     String refreshToken;
}