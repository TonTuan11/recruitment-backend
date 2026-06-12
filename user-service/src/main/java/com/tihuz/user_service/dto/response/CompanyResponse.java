package com.tihuz.user_service.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CompanyResponse {
    Long id;
    String name;
    String address;
    String category;
    String email;
}