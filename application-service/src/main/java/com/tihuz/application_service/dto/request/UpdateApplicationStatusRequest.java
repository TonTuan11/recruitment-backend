package com.tihuz.application_service.dto.request;

import com.tihuz.application_service.Enum.ApplicationsStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UpdateApplicationStatusRequest {
    @NotNull (message= "status not null")
    private ApplicationsStatus status;
}
