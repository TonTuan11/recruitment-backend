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
public class JobCreateRequest {

    @NotBlank(message = "Title is required")
    String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, message = "Description must be at least 10 characters long")
    String description;


    @NotBlank(message = "Salary is required")
    String salary;

    @NotBlank(message = "Location is required")
    String location;

    @NotBlank(message = "Experience is required")
    String experience;

    @NotNull(message = "Job type is required")
    JobType jobType;

    @NotBlank(message = "Category is required")
    String category;

    @NotNull(message = "Expired date is required")
    LocalDateTime expiredAt;


}