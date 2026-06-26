package com.tihuz.application_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tihuz.application_service.Enum.ApplicationsStatus;
import com.tihuz.application_service.client.JobClient;
import com.tihuz.application_service.client.UserClient;
import com.tihuz.application_service.dto.request.ApplyJobRequest;
import com.tihuz.application_service.dto.request.UpdateApplicationStatusRequest;
import com.tihuz.application_service.dto.response.ApplicationResponse;
import com.tihuz.application_service.dto.response.JobResponse;
import com.tihuz.application_service.dto.response.UserResponse;
import com.tihuz.application_service.entity.Application;
import com.tihuz.application_service.mapper.ApplicationMapper;
import com.tihuz.application_service.repository.ApplicationRepository;
import com.tihuz.application_service.specification.ApplicationSpecification;
import com.tihuz.common.dto.ApiResponse;
import com.tihuz.common.event.JobEvent;
import com.tihuz.common.exception.AppException;
import com.tihuz.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ApplicationService {

    ApplicationRepository applicationRepository;

    JobClient jobClient;

    ApplicationMapper applicationMapper;

    UserClient userClient;
    //+
    ObjectMapper objectMapper;

//    public ApplicationResponse apply(ApplyJobRequest request)
//    {
//        // Get current user ID
//        Long userId = getCurrentUserId();
//
//        // call feign
//        ApiResponse<JobResponse> jobResponse = jobClient.getJobById(request.getJobId());
//
//        if (jobResponse == null || jobResponse.getResult() == null)
//        {
//            throw new AppException(ErrorCode.JOB_NOT_FOUND);
//        }
//
//        // call feign user
//        ApiResponse<UserResponse> userResponse =
//                userClient.getUserById(userId);
//
//        if (userResponse == null || userResponse.getResult() == null)
//        {
//            throw new AppException(ErrorCode.USER_NOT_EXITS);
//        }
//
//        boolean exists = applicationRepository.existsByUserIdAndJobId(userId, request.getJobId());
//
//        if (exists)
//        {
//            throw new AppException(ErrorCode.APPLICATION_ALREADY_EXISTS);
//        }
//
//        Application application = applicationMapper.toApplication(request);
//        application.setUserId(userId);
//        application.setUserName(userResponse.getResult().getUsername());
//        application.setJobTitle(jobResponse.getResult().getTitle());
//        application.setStatus(ApplicationsStatus.PENDING);
//        application.setIsDeleted(false);
//
//        applicationRepository.save(application);
//
//        return applicationMapper.toApplicationResponse(application);
//    }

    //  APPLY (save snapshot JSON)
    public ApplicationResponse apply(ApplyJobRequest request)
    {
        Long userId = getCurrentUserId();

        ApiResponse<JobResponse> jobResponse = jobClient.getJobById(request.getJobId());
        if (jobResponse == null || jobResponse.getResult() == null)
            throw new AppException(ErrorCode.JOB_NOT_FOUND);

        ApiResponse<UserResponse> userResponse = userClient.getUserById(userId);
        if (userResponse == null || userResponse.getResult() == null)
            throw new AppException(ErrorCode.USER_NOT_EXITS);

        boolean exists = applicationRepository.existsByUserIdAndJobIdAndIsDeletedFalse(userId, request.getJobId());
        if (exists  )
        {
            throw new AppException(ErrorCode.APPLICATION_ALREADY_EXISTS);
        }

        // Convert JobResponse thành JSON string
        String jobSnapshotJson;
        try
        {
            jobSnapshotJson = objectMapper.writeValueAsString(jobResponse.getResult());
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to serialize job snapshot", e);
        }

        Application application = applicationMapper.toApplication(request);
        application.setUserId(userId);
        application.setUserName(userResponse.getResult().getUsername());
        application.setJobId(jobResponse.getResult().getId());
        application.setJobTitle(jobResponse.getResult().getTitle());
        application.setJobSnapshotJson(jobSnapshotJson);

        application.setStatus(ApplicationsStatus.PENDING);
        application.setIsDeleted(false);

        applicationRepository.save(application);
        return applicationMapper.toApplicationResponse(application);
    }





//    public ApplicationResponse getApplicationById(Long id)
//    {
//
//        Application app = applicationRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Application not found"));
//
//
//        ApplicationResponse response = applicationMapper.toApplicationResponse(app);
//
//        try
//        {
//            ApiResponse<UserResponse> userApiResponse = userClient.getUserById(app.getUserId());
//            if (userApiResponse != null && userApiResponse.getResult() != null)
//            {
//                response.setUser(userApiResponse.getResult());
//            }
//        }
//        catch (Exception e)
//        {
//            log.error("Failed to fetch user for userId={}", app.getUserId(), e);
//            // fallback: user = null
//        }
//
//        try
//        {
//            ApiResponse<JobResponse> jobApiResponse = jobClient.getJobById(app.getJobId());
//            if (jobApiResponse != null && jobApiResponse.getResult() != null)
//            {
//                response.setJob(jobApiResponse.getResult());
//            }
//        }
//        catch (Exception e)
//        {
//            log.error("Failed to fetch job for jobId={}", app.getJobId(), e);
//            // fallback: job = null
//        }
//
//        return response;
//    }

//    public Page<ApplicationResponse> getMyApplications(int page, int size)
//    {
//        Long userId = getCurrentUserId();
//        Pageable pageable = PageRequest.of(page, size);
//
//        // Lấy page các application chưa bị xóa của user
//        Page<Application> applicationPage = applicationRepository.findByUserIdAndIsDeletedFalse(userId, pageable);
//
//        // Dùng map đơn giản để cache job, tránh gọi nhiều lần
//        Map<Long, JobResponse> jobCache = new HashMap<>();
//
//
//        List<ApplicationResponse> responseList = new ArrayList<>();
//
//        for (Application app : applicationPage.getContent())
//        {
//            ApplicationResponse response = applicationMapper.toApplicationResponse(app);
//
//            Long jobId = app.getJobId();
//
//            JobResponse job = jobCache.get(jobId);
//            if (job == null && jobId != null)
//            {
//                ApiResponse<JobResponse> jobResp = jobClient.getJobById(jobId);
//                if (jobResp != null && jobResp.getResult() != null)
//                {
//                    job = jobResp.getResult();
//                    jobCache.put(jobId, job);
//                }
//            }
//            response.setJob(job);
//            responseList.add(response);
//        }
//
//        // Trả về Page mới
//        return new PageImpl<>(responseList, pageable, applicationPage.getTotalElements());
//
//
//        //        return applicationRepository.findByUserIdAndIsDeletedFalse(userId, pageable)




//                .map(applicationMapper::toApplicationResponse);
//    }


    // GET APPLICATION BY ID ( snapshot + current job)
    public ApplicationResponse getApplicationById(Long id)
    {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = Long.valueOf(authentication.getName());

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isCompany = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_COMPANY"));

        boolean isCompanyOwner = false;

        if (isCompany)
        {
            try {
                ApiResponse<JobResponse> job = jobClient.getJobById(app.getJobId());

                if (job != null && currentUserId.equals(job.getResult().getUserId()))
                {
                    isCompanyOwner = true;
                }
                else
                {
                    log.warn("Job owner mismatch: jobUserId={}, currentUserId={}", job != null ? job.getResult().getUserId() : null, currentUserId);
                }
            }
            catch (Exception e)
            {
                log.error("Feign call failed for jobId=" + app.getJobId(), e);
            }
        }


        if (!isAdmin && !app.getUserId().equals(currentUserId) && !isCompanyOwner)
        {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        // GET userId
        ApiResponse<UserResponse> userResponse = userClient.getUserById(app.getUserId());
        if (userResponse == null || userResponse.getResult() == null)
        {
            throw new AppException(ErrorCode.USER_NOT_EXITS);
        }


        ApplicationResponse response = applicationMapper.toApplicationResponse(app);
        response.setEmail( userResponse.getResult().getEmail());
        response.setAvatarUrl(  userResponse.getResult().getAvatarUrl());

        // Parse snapshot JSON
        try {
            JobResponse snapshot = objectMapper.readValue(app.getJobSnapshotJson(), JobResponse.class);
            response.setJobSnapshot(snapshot);
        }
        catch (Exception e)
        {
            log.error("Failed to parse job snapshot for application {}", id, e);
        }

        // Gọi Feign lấy job hiện tại (có thể null nếu job bị xóa)
        try
        {
            ApiResponse<JobResponse> currentJobResp = jobClient.getJobById(app.getJobId());
            if (currentJobResp != null && currentJobResp.getResult() != null)
            {
                response.setJob(currentJobResp.getResult());
            }
        }
        catch (Exception e)
        {
            log.error("Failed to fetch current job for jobId {}", app.getJobId(), e);
        }

        return response;
    }




//GET MY APPLICATIONS ( no call Feign)
//public Page<ApplicationResponse> getMyApplications(int page, int size)
//{
//    Long userId = getCurrentUserId();
//
//    Pageable pageable = PageRequest.of(page, size);
//    Page<Application> appPage = applicationRepository.findByUserIdAndIsDeletedFalse(userId, pageable);
//
//    return appPage.map(app ->
//    {
//        ApplicationResponse response = applicationMapper.toApplicationResponse(app);
//        try
//        {
//            if (app.getJobSnapshotJson() != null && !app.getJobSnapshotJson().isBlank())
//            {
//                JobResponse snapshot = objectMapper.readValue(app.getJobSnapshotJson(), JobResponse.class);
//                response.setJobSnapshot(snapshot);
//            }
//        }
//        catch (Exception e)
//        {
//            log.error("Failed to parse job snapshot for application {}", app.getId(), e);
//        }
//        return response;
//    });
//}


    public Page<ApplicationResponse> getMyApplications(int page, int size)
    {
        Long userId = getCurrentUserId();

        return applicationRepository
                .findByUserIdAndIsDeletedFalse(
                        userId,
                        PageRequest.of(page, size)
                )
                .map(this::mapApplicationResponse);
    }

    private ApplicationResponse mapApplicationResponse(Application app)
    {
        ApplicationResponse response = applicationMapper.toApplicationResponse(app);

        if (app.getJobSnapshotJson() == null || app.getJobSnapshotJson().isBlank())
        {
            return response;
        }

        try
        {
            response.setJobSnapshot(
                    objectMapper.readValue(
                            app.getJobSnapshotJson(),
                            JobResponse.class
                    )
            );
        }
        catch (Exception e)
        {
            log.error("Failed to parse job snapshot for application {}", app.getId(), e);
        }

        return response;
    }


    // Helper: parse snapshot JSON và set vào response
    private ApplicationResponse enrichWithSnapshot(Application app) {
        ApplicationResponse response = applicationMapper.toApplicationResponse(app);
        try
        {
            if (app.getJobSnapshotJson() != null && !app.getJobSnapshotJson().isBlank())
            {
                JobResponse snapshot = objectMapper.readValue(app.getJobSnapshotJson(), JobResponse.class);
                response.setJobSnapshot(snapshot);
            }
        }
        catch (Exception e)
        {
            log.error("Failed to parse snapshot for app {}", app.getId(), e);
        }

        // user
        ApiResponse<UserResponse> userResponse =
                userClient.getUserById(app.getUserId());

        if (userResponse != null && userResponse.getResult() != null) {
            response.setEmail(userResponse.getResult().getEmail());
            response.setAvatarUrl(userResponse.getResult().getAvatarUrl());
        }

        return response;
    }


    public boolean checkApply(Long jobId)
    {
        Long userId=Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());

    return applicationRepository.existsByUserIdAndJobIdAndIsDeletedFalse(userId,jobId);
    }


//    public List<ApplicationResponse> getApplicationsByJob(Long jobId)
//    {
//        Long userId=getCurrentUserId();
//
//        return applicationRepository.findByJobId(jobId)
//                .stream()
//                .map(applicationMapper::toApplicationResponse)
//                .toList();
//    }



//    public Page<ApplicationResponse> getApplicationByCompany(int page, int size)
//    {
//        // get user login
//        Long userId = getCurrentUserId();
//        Pageable pageable = PageRequest.of(page, size);
//
//        // Get jobId, userId, and title of the logged-in user
//        ApiResponse<Page<JobResponse>> response = jobClient.getJobsByCompanyId(userId, page, size);
//
//        if (response == null || response.getResult() == null)
//        {
//            return Page.empty(pageable);
//        }
//
//        List<JobResponse> jobs = response.getResult().getContent();
//        List<Long> jobIds = new ArrayList<>();
//
//        for (JobResponse job : jobs) {
//            jobIds.add(job.getId());
//        }
//
//        if (jobIds.isEmpty()) {
//            return Page.empty(pageable);
//        }
//
//        // CACHE
//        Map<Long, JobResponse> jobCache = new HashMap<>();
//        Map<Long, UserResponse> userCache = new HashMap<>();
//
//        return applicationRepository
//                .findByJobIdInAndIsDeletedFalse(jobIds, pageable)
//                .map(application -> mapToResponse(application, userCache, jobCache));
//    }



    public Page<ApplicationResponse> getApplicationByCompany(int page, int size)
    {
        Long userId = getCurrentUserId();

        Pageable pageable = PageRequest.of(page, size);

        ApiResponse<Page<JobResponse>> response = jobClient.getJobsByCompanyId(userId, page, size);


        if (response == null || response.getResult() == null)
        {
            return Page.empty(pageable);
        }



        List<Long> jobIds = response.getResult().getContent().stream()
                .map(JobResponse::getId)
                .toList();

        if (jobIds.isEmpty())
        {
            return Page.empty(pageable);
        }

        return applicationRepository.findByJobIdInAndIsDeletedFalse(jobIds, pageable)
                .map(this::enrichWithSnapshot);
    }

    public ApplicationResponse updateStatus(Long applicationId, UpdateApplicationStatusRequest request) {
        Long userId = getCurrentUserId();
        System.out.println("=== Current userId: " + userId);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));
        System.out.println("=== Application jobId: " + application.getJobId());

        // Gọi API lấy danh sách job của công ty
        ApiResponse<Page<JobResponse>> response = jobClient.getJobsByCompanyId(userId, 0, 100);

        if (response == null || response.getResult() == null) {
            System.out.println("=== Response is null or empty");
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }

        List<Long> jobIds = response.getResult().getContent().stream()
                .map(JobResponse::getId)
                .toList();
        System.out.println("=== Jobs owned by user: " + jobIds);
        System.out.println("=== Is jobId " + application.getJobId() + " in list? " + jobIds.contains(application.getJobId()));

        if (!jobIds.contains(application.getJobId())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        application.setStatus(request.getStatus());
        applicationRepository.save(application);
        return applicationMapper.toApplicationResponse(application);
    }

//    private ApplicationResponse mapToResponse(Application application )
//    {
//
//
//        ApplicationResponse response=applicationMapper.toApplicationResponse(application);
//
//        response.setUser(
//                userClient.getUserById(application.getUserId())
//                        .getResult() );
//
//        response.setJob(
//                jobClient.getJobById(application.getJobId())
//                        .getResult()
//
//        );
//return response;
//
//    }


    private Long getCurrentUserId()
    {
        Long userId=Long.valueOf(SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName());
    return userId;
    }

    private ApplicationResponse mapToResponse(
            Application application,
            Map<Long, UserResponse> userCache,
            Map<Long, JobResponse> jobCache
    )
    {
        ApplicationResponse response = applicationMapper.toApplicationResponse(application);

        UserResponse user = userCache.computeIfAbsent(
                application.getUserId(),
                id -> userClient.getUserById(id).getResult()
        );

        JobResponse job = jobCache.computeIfAbsent(
                application.getJobId(),
                id -> jobClient.getJobById(id).getResult()
        );

        response.setUser(user);
        response.setJob(job);

        return response;
    }

    public long countApplicationsByJob(Long jobId)
    {
        return applicationRepository.countByJobIdAndIsDeletedFalse(jobId);
    }

    public long countPendingApplicationsByJob(Long jobId)
    {
        return applicationRepository.countByJobIdAndStatusAndIsDeletedFalse(jobId, ApplicationsStatus.PENDING);
    }

//    public Page<ApplicationResponse> getApplicationsByJob(Long jobId, int page, int size)
//    {
//
//        Pageable pageable = PageRequest.of(page, size);
//
//        // Lấy thông tin job
//        ApiResponse<JobResponse> jobResponse = jobClient.getJobById(jobId);
//        JobResponse job = jobResponse.getResult();
//
//        if (job == null) {
//            throw new AppException(ErrorCode.JOB_NOT_FOUND);
//        }
//
//        // Lấy danh sách ứng viên
//        Page<Application> applications = applicationRepository.findByJobIdAndIsDeletedFalse(jobId, pageable);
//
//        Map<Long, UserResponse> userCache = new HashMap<>();
//        Map<Long, JobResponse> jobCache = new HashMap<>();
//
//        return applications.map(application -> mapToResponse(application, userCache, jobCache));
//    }


    public Page<ApplicationResponse> getApplicationsByJob(Long jobId, int page, int size)
    {
        Long currentUserId = getCurrentUserId();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin)
        {
            ApiResponse<JobResponse> jobResp = jobClient.getJobById(jobId);
            if (jobResp == null || jobResp.getResult() == null)
            {
                throw new AppException(ErrorCode.JOB_NOT_FOUND);
            }
            if (!currentUserId.equals(jobResp.getResult().getUserId()))
            {
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }
        }


        Pageable pageable = PageRequest.of(page, size);

    Page<Application> applications = applicationRepository.findByJobIdAndIsDeletedFalse(jobId, pageable);
    return applications.map(this::enrichWithSnapshot);
    }


    public long countApplicationsByCompany(Long userId)
    {
        // Lấy danh sách job của công ty
        ApiResponse<Page<JobResponse>> response = jobClient.getJobsByCompanyId(userId, 0, Integer.MAX_VALUE);

        if (response == null || response.getResult() == null) {
            return 0;
        }

        List<Long> jobIds = response.getResult().getContent()
                .stream()
                .map(JobResponse::getId)
                .toList();

        if (jobIds.isEmpty())
        {
            return 0;
        }

        return applicationRepository.countByJobIdInAndIsDeletedFalse(jobIds);
    }


//    public Page<ApplicationResponse> getActiveApplications(Pageable pageable, Long jobId, Long userId)
//    {
//        Page<Application> page;
//        if (jobId != null)
//        {
//            page = applicationRepository.findByIsDeletedFalseAndJobId(jobId, pageable);
//        }
//        else if (userId != null)
//        {
//            page = applicationRepository.findByIsDeletedFalseAndUserId(userId, pageable);
//        }
//        else
//        {
//            page = applicationRepository.findByIsDeletedFalse(pageable);
//        }
//        return page.map(application -> {
//            // Có thể enrich job và user ở đây hoặc để FE gọi riêng
//            ApplicationResponse response = applicationMapper.toApplicationResponse(application);
//            // Nếu muốn enrich, gọi Feign
//            return response;
//        });
//    }

//    public Page<ApplicationResponse> getActiveApplications(Pageable pageable,  String userName, String jobTitle)
//    {
//
//        return applicationRepository.findAll(
//                ApplicationSpecification.filter(false, userName, jobTitle), pageable)
//                .map(applicationMapper::toApplicationResponse);
//    }

    // GET ACTIVE APPLICATIONS (admin, dùng snapshot)
    public Page<ApplicationResponse> getActiveApplications(Pageable pageable, String userName, String jobTitle)
    {
        return applicationRepository.findAll(
                        ApplicationSpecification.filter(false, userName, jobTitle), pageable)
                .map(applicationMapper::toApplicationResponse);
    }


    // GET DELETED APPLICATIONS (admin)
    public Page<ApplicationResponse> getDeleteApplications(Pageable pageable, String userName, String jobTitle)
    {
        return applicationRepository.findAll(
                ApplicationSpecification.filter(true, userName, jobTitle), pageable)
                .map(applicationMapper::toApplicationResponse);
    }



//    public Page<ApplicationResponse> getDeleteApplications(Pageable pageable, Long jobId, Long userId)
//    {
//        Page<Application> page;
//        if (jobId != null)
//        {
//            page = applicationRepository.findByIsDeletedTrueAndJobId(jobId, pageable);
//        }
//        else if (userId != null)
//        {
//            page = applicationRepository.findByIsDeletedTrueAndUserId(userId, pageable);
//        }
//        else
//        {
//            page = applicationRepository.findByIsDeletedTrue(pageable);
//        }
//        return page.map(application -> {
//            // Có thể enrich job và user ở đây hoặc để FE gọi riêng
//            ApplicationResponse response = applicationMapper.toApplicationResponse(application);
//            // Nếu muốn enrich, gọi Feign
//            return response;
//        });
//    }

    public void restoreApplication(Long id)
    {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));
        application.setIsDeleted(false);
        applicationRepository.save(application);
    }

    @Transactional
    public ApplicationResponse reapply(Long oldApplicationId,
                                       ApplyJobRequest request)
    {
        // Tìm đơn cũ
        Application oldApp = applicationRepository.findById(oldApplicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));

        // Lấy user hiện tại từ JWT (sub)
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long currentUserId = Long.valueOf(authentication.getName());

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Chỉ chủ đơn hoặc admin mới được apply lại
        if (!isAdmin && !oldApp.getUserId().equals(currentUserId))
        {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        // Soft delete đơn cũ
        oldApp.setIsDeleted(true);
        applicationRepository.save(oldApp);

        // Tạo request mới
        ApplyJobRequest newRequest = new ApplyJobRequest();
        newRequest.setJobId(oldApp.getJobId());

        if (request != null)
        {
            newRequest.setCvUrl(
                    request.getCvUrl() != null && !request.getCvUrl().isBlank()
                            ? request.getCvUrl()
                            : oldApp.getCvUrl());

            newRequest.setCoverLetter(
                    request.getCoverLetter() != null && !request.getCoverLetter().isBlank()
                            ? request.getCoverLetter()
                            : oldApp.getCoverLetter());
        }
        else
        {
            newRequest.setCvUrl(oldApp.getCvUrl());
            newRequest.setCoverLetter(oldApp.getCoverLetter());
        }

        // Tạo đơn mới
        return apply(newRequest);
    }

    @Transactional
    public void softDeleteApplication(Long id)
    {
        Long userId= getCurrentUserId();

        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));

        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !app.getUserId().equals(userId))
        {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        app.setIsDeleted(true);
        applicationRepository.save(app);
    }


    @Transactional
    public void hardDeleteApplication(Long id) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));
        applicationRepository.delete(app);
        log.info("Hard deleted application with id: {}", id);
    }


    @Transactional
    public void deleteApplicationsByJobId(Long jobId)
    {
        List<Application> applications = applicationRepository.findByJobId(jobId);
        if (!applications.isEmpty())
        {
            applicationRepository.deleteAll(applications);
            log.info("Deleted {} applications for job {}", applications.size(), jobId);
        }
    }


//    // This method listens for events from Job Service
//    @KafkaListener(topics = JobEvent.TOPIC, groupId = "application-service-group")
//    public void handleJobEvent(JobEvent event)
//    {
//        log.info("Application Service received JobEvent: Action={}, JobId={}", event.getAction(), event.getJobId());
//
//        switch (event.getAction())
//         {
//            case "SOFT_DELETED":
//                if (event.getJobId() != null) {
//                    List<Application> applications = applicationRepository.findByJobIdAndIsDeletedFalse(event.getJobId());
//                    for (Application app : applications) {
//                        app.setIsDeleted(true);
//                    }
//                    applicationRepository.saveAll(applications);
//                    log.info("Soft deleted {} applications for job {}", applications.size(), event.getJobId());
//                }
//                break;
//
////            case "HARD_DELETED":
////                if (event.getJobId() != null) {
////                    // Lấy tất cả application (kể cả đã soft delete) của job này
////                    List<Application> allApps = applicationRepository.findByJobId(event.getJobId());
////                    applicationRepository.deleteAll(allApps);
////                    log.info("Hard deleted {} applications for job {}", allApps.size(), event.getJobId());
////                }
////                break;
//
//             default:
//                 log.debug("No action needed for job event: {}", event.getAction());
//         }
//
//
//    }


}