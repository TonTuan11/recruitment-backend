package com.tihuz.application_service.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyResponse
{
     Long id;
     String name;
     String logo;
     String email;
     String website;
     String address;
     String category;
     String description;
     String scale;
}