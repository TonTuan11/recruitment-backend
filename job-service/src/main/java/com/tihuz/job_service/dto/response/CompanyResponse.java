package com.tihuz.job_service.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)

public class CompanyResponse {

    Long id;
    String name;
    String logo;
    String category;
}
