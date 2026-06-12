package com.tihuz.application_service.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobResponse {

     Long id;
     Long userId;
     Long companyId;
     String companyName;
     String companyLogo;
     String title;
     String description;
     String salary;
     String location;
     String experience;
     String jobType;
     String category;
     LocalDateTime expiredAt;
     LocalDateTime createdAt;
     LocalDateTime updatedAt;
}