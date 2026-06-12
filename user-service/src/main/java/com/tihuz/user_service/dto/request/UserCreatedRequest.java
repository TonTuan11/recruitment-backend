package com.tihuz.user_service.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreatedRequest {

    @NotBlank(message = "Username must not be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username;

    String companyId;

    @JsonProperty("isCompanyRegister")
    boolean isCompanyRegister;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    String email;

    @Pattern(
            regexp = "^(|https?://.*)$",
            message = "Avatar must be a valid URL"
    )
    String avatarUrl;


    String companyName;
    String companyAddress;
    String companyCategory;

}
