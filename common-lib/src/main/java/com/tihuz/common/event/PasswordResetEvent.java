package com.tihuz.common.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordResetEvent {
    public static final String TOPIC = "password-reset-events";

     Long userId;
     String email;
     String username;
     String otp;
}