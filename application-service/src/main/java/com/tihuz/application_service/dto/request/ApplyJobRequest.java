package com.tihuz.application_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplyJobRequest {
    @NotNull(message ="jobId not null")
    Long jobId;

    @NotBlank (message = "cvUrl not null")
    String cvUrl;

    @Size(max = 1000)
    String coverLetter;
}