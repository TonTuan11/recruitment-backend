package com.tihuz.application_service.controller;

import com.tihuz.application_service.dto.request.UpdateApplicationStatusRequest;
import com.tihuz.application_service.dto.response.ApplicationResponse;
import com.tihuz.application_service.dto.request.ApplyJobRequest;

import com.tihuz.application_service.service.ApplicationService;
import com.tihuz.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ApiResponse<ApplicationResponse> apply(@RequestBody @Valid ApplyJobRequest request)
    {

        return ApiResponse.<ApplicationResponse>builder()
                .result(applicationService.apply(request))
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<Page<ApplicationResponse>> getMine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    )
    {

        return ApiResponse.<Page<ApplicationResponse>>builder()
                .result(applicationService.getMyApplications(page,size))
                .build();
    }



    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ApplicationResponse> getApplicationById(
            @PathVariable Long id)
    {
        return ApiResponse.<ApplicationResponse>builder()
                .result(applicationService.getApplicationById(id))
                .build();
    }

//    @GetMapping("/job/{jobId}")
//    public ApiResponse<List<ApplicationResponse>> getByJob(
//            @PathVariable Long jobId
//    )
//    {
//        return ApiResponse.<List<ApplicationResponse>>builder()
//                .result(applicationService.getApplicationsByJob(jobId))
//                .build();
//    }

    @GetMapping("/company")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMPANY')")
    public ApiResponse<Page<ApplicationResponse>> getApplicationCompany( @RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "10") int size)
    {

        return ApiResponse.<Page<ApplicationResponse>>builder()
                .result(applicationService.getApplicationByCompany(page, size))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    @PutMapping("/{id}/status")
    public ApiResponse<ApplicationResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateApplicationStatusRequest request,
            @RequestParam Long userId)
    {
        return ApiResponse.<ApplicationResponse>builder()
                .result(applicationService.updateStatus(id, request))
                .build();
    }


    @GetMapping("/check-apply")
    public boolean checkApply(@RequestParam Long jobId)
    {
        return applicationService.checkApply(jobId);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    @GetMapping("/count-by-job/{jobId}")
    public ApiResponse<Long> countApplicationsByJob(@PathVariable Long jobId,  @RequestParam Long userId){
        long count = applicationService.countApplicationsByJob(jobId);
        return ApiResponse.<Long>builder()
                .result(count)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    @GetMapping("/count-pending-by-job/{jobId}")
    public ApiResponse<Long> countPendingApplicationsByJob(@PathVariable Long jobId, @RequestParam Long userId) {
        long count = applicationService.countPendingApplicationsByJob(jobId);
        return ApiResponse.<Long>builder()
                .result(count)
                .build();
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('COMPANY')")
    @GetMapping("/job/{jobId}")
    public ApiResponse<Page<ApplicationResponse>> getApplicationsByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)

    {
        return ApiResponse.<Page<ApplicationResponse>>builder()
                .result(applicationService.getApplicationsByJob(jobId, page, size))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active")
    public ApiResponse<Page<ApplicationResponse>> getActiveApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)  String userName,
            @RequestParam(required = false) String jobTitle)
    {
        Pageable pageable = PageRequest.of(page, size);

        return ApiResponse.<Page<ApplicationResponse>>builder()
                .result(applicationService.getActiveApplications(pageable,  userName,jobTitle))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/deleted")
    public ApiResponse<Page<ApplicationResponse>> getDeletedApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,

            @RequestParam(required = false)  String userName,
            @RequestParam(required = false) String jobTitle)
    {
        Pageable pageable = PageRequest.of(page, size);

        return ApiResponse.<Page<ApplicationResponse>>builder()
                .result(applicationService.getDeleteApplications(pageable, userName,jobTitle))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/restore/{applicationId}")
    public ApiResponse<Void> restoreApplication(@PathVariable Long applicationId)
    {
        applicationService.restoreApplication(applicationId);

        return ApiResponse.<Void>builder()
                .message("Khôi phục hồ sơ ứng tuyển thành công")
                .build();
    }


    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    @GetMapping("/count-by-company")
    public ApiResponse<Long> countApplicationsByCompany(@RequestParam Long userId) {
        long count = applicationService.countApplicationsByCompany(userId);
        return ApiResponse.<Long>builder()
                .result(count)
                .build();
    }


    @PostMapping("/{id}/reapply")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ApiResponse<ApplicationResponse> reapply(
            @PathVariable Long id,
            @RequestBody(required = false) ApplyJobRequest request)
    {
        return ApiResponse.<ApplicationResponse>builder()
                .result(applicationService.reapply(id, request))
                .build();
    }



    @DeleteMapping("/soft/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ApiResponse<Void> softDeleteApplication(@PathVariable Long id) {
        applicationService.softDeleteApplication(id);
        return ApiResponse.<Void>builder().message("Đã rút đơn ứng tuyển").build();
    }


    @DeleteMapping("/hard/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> hardDeleteApplication(@PathVariable Long id)
    {
        applicationService.hardDeleteApplication(id);
        return ApiResponse.<Void>builder()
                .message("Đã xóa cứng đơn ứng tuyển")
                .build();
    }

    @DeleteMapping("/by-job/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteApplicationsByJobId(@PathVariable Long jobId) {
        applicationService.deleteApplicationsByJobId(jobId);
        return ApiResponse.<Void>builder()
                .message("Deleted all applications for job " + jobId)
                .build();
    }

}