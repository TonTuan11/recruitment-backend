package com.tihuz.company_service.dto;


import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyRequest {


    String name;

    String logo;

    String website;
    String email;
    String scale;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    String description;

    String address;

    String category;
}