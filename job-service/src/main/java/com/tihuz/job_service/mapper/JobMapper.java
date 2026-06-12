package com.tihuz.job_service.mapper;

import com.tihuz.job_service.dto.request.JobCreateRequest;
import com.tihuz.job_service.dto.response.JobResponse;

import com.tihuz.job_service.entity.Job;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JobMapper {

    Job toJob(JobCreateRequest request);

//    @Mapping(source = "id", target = "id")
    JobResponse toJobResponse(Job job);

    List<JobResponse> toJobResponseList(List<Job> jobs);
}
