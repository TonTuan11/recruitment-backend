package com.tihuz.job_service.dto.request;

import com.tihuz.job_service.Enum.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobUpdateRequest {

    String title;


    @Size(min = 10, message = "Description must be at least 10 characters long")
    String description;


    String salary;


    String location;


    String experience;


    JobType jobType;


    String category;


    LocalDateTime expiredAt;
}
