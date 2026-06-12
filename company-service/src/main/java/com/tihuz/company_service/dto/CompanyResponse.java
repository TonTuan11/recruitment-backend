package com.tihuz.company_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CompanyResponse {
    Long id;
    String name;
    String logo;
    String website;
    String email;
    String scale;
    String description;
    String address;
    String category;
    Integer totalJobs;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}