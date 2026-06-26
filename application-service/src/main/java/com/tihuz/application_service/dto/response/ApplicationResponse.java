package com.tihuz.application_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.tihuz.application_service.Enum.ApplicationsStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationResponse {

    Long id;

    Long userId;
    String userName;
    String email;
    String avatarUrl;

    Long jobId;
    String jobTitle;


    String cvUrl;

    String coverLetter;

    ApplicationsStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;

    // enrich by user-service
    UserResponse user;

    // enrich by job-service
    JobResponse job;

    // +: snapshot đầy đủ (JobResponse)
    JobResponse jobSnapshot;


}