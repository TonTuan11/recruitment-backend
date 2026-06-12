package com.tihuz.job_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tihuz.job_service.Enum.JobType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    JobType jobType;

    String category;

    LocalDateTime expiredAt;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
    Long views;

}
