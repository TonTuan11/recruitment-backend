package com.tihuz.application_service.mapper;

import com.tihuz.application_service.dto.response.ApplicationResponse;
import com.tihuz.application_service.dto.request.ApplyJobRequest;
import com.tihuz.application_service.entity.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApplicationMapper
{
    Application toApplication (ApplyJobRequest applyJobRequest);

   // @Mapping(target = "status", expression = "java(app.getStatus().name())")
    ApplicationResponse toApplicationResponse(Application app);
}