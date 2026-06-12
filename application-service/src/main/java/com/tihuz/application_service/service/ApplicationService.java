package com.tihuz.application_service.service;

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
import com.tihuz.common.dto.ApiResponse;
import com.tihuz.common.event.JobEvent;
import com.tihuz.common.exception.AppException;
import com.tihuz.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;
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

    public ApplicationResponse apply(ApplyJobRequest request)
    {
        // Get current user ID
        Long userId = getCurrentUserId();

        // check job exists
        ApiResponse<JobResponse> jobResponse = jobClient.getJobById(request.getJobId());

        if (jobResponse == null || jobResponse.getResult() == null) {
            throw new AppException(ErrorCode.JOB_NOT_FOUND);
        }

        boolean exists = applicationRepository.existsByUserIdAndJobId(userId, request.getJobId());

        if (exists) {
            throw new AppException(ErrorCode.APPLICATION_ALREADY_EXISTS);
        }

        Application application = applicationMapper.toApplication(request);
        application.setUserId(userId);
        application.setStatus(ApplicationsStatus.PENDING);
        application.setIsDeleted(false);

        applicationRepository.save(application);

        return applicationMapper.toApplicationResponse(application);
    }

    public Page<ApplicationResponse> getMyApplications(int page, int size)
    {
        Long userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);

        // Lấy page các application chưa bị xóa của user
        Page<Application> applicationPage = applicationRepository.findByUserIdAndIsDeletedFalse(userId, pageable);

        // Dùng map đơn giản để cache job, tránh gọi nhiều lần
        Map<Long, JobResponse> jobCache = new HashMap<>();


        List<ApplicationResponse> responseList = new ArrayList<>();

        for (Application app : applicationPage.getContent())
        {
            ApplicationResponse response = applicationMapper.toApplicationResponse(app);

            Long jobId = app.getJobId();

            JobResponse job = jobCache.get(jobId);
            if (job == null && jobId != null)
            {
                ApiResponse<JobResponse> jobResp = jobClient.getJobById(jobId);
                if (jobResp != null && jobResp.getResult() != null)
                {
                    job = jobResp.getResult();
                    jobCache.put(jobId, job);
                }
            }
            response.setJob(job);
            responseList.add(response);
        }

        // Trả về Page mới
        return new PageImpl<>(responseList, pageable, applicationPage.getTotalElements());


        //        return applicationRepository.findByUserIdAndIsDeletedFalse(userId, pageable)
//                .map(applicationMapper::toApplicationResponse);
    }


    public boolean checkApply(Long jobId)
    {
        Long userId=Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());

    return applicationRepository.existsByUserIdAndJobId(userId,jobId);
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

    public Page<ApplicationResponse> getApplicationByCompany(int page, int size) {
        // get user login
        Long userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);

        // Get jobId, userId, and title of the logged-in user
        ApiResponse<Page<JobResponse>> response = jobClient.getJobsByCompanyId(userId, page, size);

        if (response == null || response.getResult() == null) {
            return Page.empty(pageable);
        }

        List<JobResponse> jobs = response.getResult().getContent();
        List<Long> jobIds = new ArrayList<>();

        for (JobResponse job : jobs) {
            jobIds.add(job.getId());
        }

        if (jobIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // CACHE
        Map<Long, JobResponse> jobCache = new HashMap<>();
        Map<Long, UserResponse> userCache = new HashMap<>();

        return applicationRepository
                .findByJobIdInAndIsDeletedFalse(jobIds, pageable)
                .map(application -> mapToResponse(application, userCache, jobCache));
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

    public Page<ApplicationResponse> getApplicationsByJob(Long jobId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        // Lấy thông tin job
        ApiResponse<JobResponse> jobResponse = jobClient.getJobById(jobId);
        JobResponse job = jobResponse.getResult();

        if (job == null) {
            throw new AppException(ErrorCode.JOB_NOT_FOUND);
        }

        // Lấy danh sách ứng viên
        Page<Application> applications = applicationRepository.findByJobIdAndIsDeletedFalse(jobId, pageable);

        Map<Long, UserResponse> userCache = new HashMap<>();
        Map<Long, JobResponse> jobCache = new HashMap<>();

        return applications.map(application -> mapToResponse(application, userCache, jobCache));
    }

    public long countApplicationsByCompany(Long userId) {
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


    public Page<ApplicationResponse> getActiveApplications(Pageable pageable, Long jobId, Long userId)
    {
        Page<Application> page;
        if (jobId != null)
        {
            page = applicationRepository.findByIsDeletedFalseAndJobId(jobId, pageable);
        }
        else if (userId != null)
        {
            page = applicationRepository.findByIsDeletedFalseAndUserId(userId, pageable);
        }
        else
        {
            page = applicationRepository.findByIsDeletedFalse(pageable);
        }
        return page.map(application -> {
            // Có thể enrich job và user ở đây hoặc để FE gọi riêng
            ApplicationResponse response = applicationMapper.toApplicationResponse(application);
            // Nếu muốn enrich, gọi Feign
            return response;
        });
    }


    public Page<ApplicationResponse> getDeleteApplications(Pageable pageable, Long jobId, Long userId)
    {
        Page<Application> page;
        if (jobId != null)
        {
            page = applicationRepository.findByIsDeletedTrueAndJobId(jobId, pageable);
        }
        else if (userId != null)
        {
            page = applicationRepository.findByIsDeletedTrueAndUserId(userId, pageable);
        }
        else
        {
            page = applicationRepository.findByIsDeletedTrue(pageable);
        }
        return page.map(application -> {
            // Có thể enrich job và user ở đây hoặc để FE gọi riêng
            ApplicationResponse response = applicationMapper.toApplicationResponse(application);
            // Nếu muốn enrich, gọi Feign
            return response;
        });
    }

    public void restoreApplication(Long id)
    {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));
        application.setIsDeleted(false);
        applicationRepository.save(application);
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


    // This method listens for events from Job Service
    @KafkaListener(topics = JobEvent.TOPIC, groupId = "application-service-group")
    public void handleJobEvent(JobEvent event)
    {
        log.info("Application Service received JobEvent: Action={}, JobId={}", event.getAction(), event.getJobId());

        switch (event.getAction())
         {
            case "SOFT_DELETED":
                if (event.getJobId() != null) {
                    List<Application> applications = applicationRepository.findByJobIdAndIsDeletedFalse(event.getJobId());
                    for (Application app : applications) {
                        app.setIsDeleted(true);
                    }
                    applicationRepository.saveAll(applications);
                    log.info("Soft deleted {} applications for job {}", applications.size(), event.getJobId());
                }
                break;

//            case "HARD_DELETED":
//                if (event.getJobId() != null) {
//                    // Lấy tất cả application (kể cả đã soft delete) của job này
//                    List<Application> allApps = applicationRepository.findByJobId(event.getJobId());
//                    applicationRepository.deleteAll(allApps);
//                    log.info("Hard deleted {} applications for job {}", allApps.size(), event.getJobId());
//                }
//                break;

             default:
                 log.debug("No action needed for job event: {}", event.getAction());
         }


    }


}