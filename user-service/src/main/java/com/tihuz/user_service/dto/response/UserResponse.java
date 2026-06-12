package com.tihuz.user_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tihuz.user_service.Enum.RoleType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse
{
    Long id;
    String username;
    Long companyId;
    RoleType role;

    String email;
    String avatarUrl;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
