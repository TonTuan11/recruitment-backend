package com.tihuz.job_service.controller;

import com.tihuz.common.dto.ApiResponse;
import com.tihuz.job_service.dto.request.JobCreateRequest;
import com.tihuz.job_service.dto.request.JobUpdateRequest;
import com.tihuz.job_service.dto.response.CompanyResponse;
import com.tihuz.job_service.dto.response.JobResponse;
import com.tihuz.job_service.service.JobService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RequestMapping("/jobs")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal=true)
public class JobController {

    JobService jobService;

    @PreAuthorize("hasRole('COMPANY') or hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<List<JobResponse>> create(@RequestBody @Valid JobCreateRequest request)
    {
        return ApiResponse.<List<JobResponse>>builder()
                .result( List.of(jobService.createJob(request)))
                .build();

    }


    @GetMapping
    public  ApiResponse<List<JobResponse>> getAll()
    {
        return ApiResponse.<List<JobResponse>>builder()
                .result( jobService.getAll())
                .build();
    }

    @GetMapping("/types")
    public ApiResponse<List<String>>  getJobType()
    {
        return ApiResponse.<List<String>>builder()
                .result( jobService.getJobType())
                .build();

    }

    @GetMapping("/category")
    public ApiResponse<List<String>>  getCategory()
    {
        return ApiResponse.<List<String>>builder()
                .result( jobService.getCategory())
                .build();

    }

    @GetMapping("/experience")
    public ApiResponse<List<String>>  getExperience()
    {
        return ApiResponse.<List<String>>builder()
                .result( jobService.getExperience())
                .build();

    }


//    @GetMapping("/company-name")
//    public ApiResponse<List<String>>  getCompanyName()
//    {
//        return ApiResponse.<List<String>>builder()
//                .result( jobService.getCompanyName())
//                .build();
//
//    }

    @GetMapping("/title")
    public ApiResponse<List<String>>  getTitle()
    {
        return ApiResponse.<List<String>>builder()
                .result( jobService.getTitle())
                .build();

    }

    @GetMapping("/location")
    public ApiResponse<List<String>>  getLocation()
    {

        return ApiResponse.<List<String>>builder()
                .result( jobService.getLocation())
                .build();

    }

    @GetMapping("/salary")
    public ApiResponse<List<String>>  getSalary()
    {

        return ApiResponse.<List<String>>builder()
                .result( jobService.getSalary())
                .build();

    }


    @GetMapping("{id}")
    public  ApiResponse<JobResponse> getId(@PathVariable Long id)
    {
        return ApiResponse.<JobResponse>builder()
                .result( jobService.getById(id))
                .build();
    }

    @GetMapping("/job-types-default")
    public ApiResponse<List<String>>  getJobTypesDefault()
    {
        return ApiResponse.<List<String>>builder()
                .result( jobService.getJobTypesDefault())
                .build();

    }


//    @GetMapping("/count/{userId}")
//    public ResponseEntity<Long> countJobs(@PathVariable Long userId) {
//        long count = jobService.countJobsByUserId(userId);
//        return ResponseEntity.ok(count);
//    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('COMPANY') or #userId.toString() == authentication.name")
    @GetMapping("/user/{userId}")
    public ApiResponse<Page<JobResponse>> getByUser(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @PathVariable Long userId)
    {
        return ApiResponse.<Page<JobResponse>>builder()
                .result(jobService.getByUser(page,size,userId))
                .build();

    }

    @GetMapping("/related") public ApiResponse<List<JobResponse>> getRelatedJobs( @RequestParam Long jobId )
    {
        return ApiResponse.<List<JobResponse>>builder()
                .result(jobService.getRelatedJobs(jobId))
                .build();
    }

    @GetMapping("/check-apply") public boolean checkApply( @RequestParam Long jobId )
    {
       return jobService.checkApply(jobId);
    }


    @GetMapping("/paged")
    public ApiResponse<Page<JobResponse>> getJobsPaged( @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String location,
                                              @RequestParam(required = false) String experience,
                                           @RequestParam(required = false) String category,
                                             @RequestParam(required = false) String jobType,
                                             @RequestParam(required = false) Long companyId,
                                             @RequestParam(required = false) Long userId)
    {
        return ApiResponse.<Page<JobResponse>>builder()
                .result( jobService.getJobPaged( page, size, keyword, location, experience, category, jobType,companyId, userId))
                .build();

    }



    @GetMapping("/company-name/{companyId}")
    public ApiResponse<CompanyResponse> testGetCompanyName(@PathVariable Long companyId)
    {

        return jobService.getCompanyName(companyId);

    }


    @GetMapping("/count-by-company")
    public ApiResponse<Long> countJobsByCompany(@RequestParam Long companyId) {
        long count = jobService.countJobsByCompany(companyId);
        return ApiResponse.<Long>builder()
                .result(count)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    @PutMapping("/{id}")
    public ApiResponse<JobResponse> updateJob(@PathVariable Long id,@RequestBody JobUpdateRequest request , @RequestParam Long userId)
    {
        return ApiResponse.<JobResponse>builder()
                        .result(jobService.updateJob(id,request))
                         .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active")
    public ApiResponse<Page<JobResponse>> getActiveJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword)
    {
        Pageable pageable = PageRequest.of(page, size);

        return ApiResponse.<Page<JobResponse>>builder()
                .result(jobService.getActiveJobs(pageable, keyword))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/deleted")
    public ApiResponse<Page<JobResponse>> getDeletedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword)
    {
        Pageable pageable = PageRequest.of(page, size);

        return ApiResponse.<Page<JobResponse>>builder()
                .result(jobService.getDeletedJobs(pageable, keyword))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/restore/{jobId}")
    public ApiResponse<Void> restoreJob(@PathVariable Long jobId)
    {
        jobService.restoreJob(jobId);

        return ApiResponse.<Void>builder()
                .message("Khôi phục công việc thành công")
                .build();
    }



    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    @DeleteMapping("/soft/{id}")
    public ApiResponse<Void> deleteSoftJob(@PathVariable Long id, @RequestParam Long userId) {
        jobService.deleteJob(id);
        return ApiResponse.<Void>builder()
                .message("delete success")
                .build();
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/hard/{id}")
    public ApiResponse<Void> deleteJob(@PathVariable Long id,  @RequestParam Long userId)
    {
        jobService.hardDeleteJob(id);
       return ApiResponse.<Void>builder()
               .message("delete success ")
               .build();
    }
}
