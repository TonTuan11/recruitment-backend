package com.tihuz.job_service.service;

import com.tihuz.common.dto.ApiResponse;
import com.tihuz.common.event.CompanyEvent;
import com.tihuz.common.event.JobEvent;
import com.tihuz.common.exception.AppException;
import com.tihuz.common.exception.ErrorCode;
import com.tihuz.job_service.Enum.JobType;
import com.tihuz.job_service.client.ApplicationClient;
import com.tihuz.job_service.client.CompanyClient;
import com.tihuz.job_service.client.UserClient;
import com.tihuz.job_service.dto.request.JobCreateRequest;
import com.tihuz.job_service.dto.request.JobUpdateRequest;
import com.tihuz.job_service.dto.response.CompanyResponse;
import com.tihuz.job_service.dto.response.JobResponse;
import com.tihuz.job_service.dto.response.UserResponse;
import com.tihuz.job_service.entity.Job;
import com.tihuz.job_service.mapper.JobMapper;
import com.tihuz.job_service.repository.JobRepository;
import com.tihuz.job_service.specification.JobSpecification;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobService {

    JobRepository jobRepository;
    JobMapper jobMapper;
    UserClient userClient;
    CompanyClient companyClient;
    ApplicationClient applicationClient;
    KafkaTemplate<String, Object> kafkaTemplate;
    HttpServletRequest httpServletRequest;

    public JobResponse createJob(JobCreateRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.valueOf(authentication.getName());

        String token = httpServletRequest.getHeader("Authorization");

        if (request.getExpiredAt() != null && request.getExpiredAt().isBefore(LocalDateTime.now()))
        {
            throw new AppException(ErrorCode.EXPIRED_DATE_INVALID);
        }

        ApiResponse<UserResponse> userResponse = userClient.getUserById(token, userId);
        UserResponse user = userResponse.getResult();
        Long companyId = user.getCompanyId();
        if (companyId == null) {
            throw new AppException(ErrorCode.COMPANY_NOT_FOUND);
        }


        ApiResponse<CompanyResponse> companyResponse = getCompanyName(companyId);
        CompanyResponse company = companyResponse.getResult();

        if (company == null) {
            throw new AppException(ErrorCode.COMPANY_NOT_FOUND);
        }


        var job = jobMapper.toJob(request);
        job.setUserId(userId);
        job.setCompanyId(companyId);
        job.setCompanyName(company.getName());
        job.setCompanyLogo(company.getLogo());
        job.setCategory(company.getCategory());

        var saved = jobRepository.save(job);


// Publish job created event
        JobEvent jobEvent = new JobEvent();
        jobEvent.setEventId(UUID.randomUUID().toString());
        jobEvent.setEventType("JOB_CREATED");
        jobEvent.setSource("job-service");
        jobEvent.setJobId(saved.getId());
        jobEvent.setCompanyId(saved.getCompanyId());
        jobEvent.setAction("CREATED");

// Thêm đầy đủ các field
        jobEvent.setTitle(saved.getTitle());
        jobEvent.setDescription(saved.getDescription());
        jobEvent.setSalary(saved.getSalary());
        jobEvent.setLocation(saved.getLocation());
        jobEvent.setExperience(saved.getExperience());
        jobEvent.setJobType(saved.getJobType() != null ? saved.getJobType().name() : null);
        jobEvent.setCategory(saved.getCategory());
        jobEvent.setExpiredAt(saved.getExpiredAt());

        kafkaTemplate.send(JobEvent.TOPIC, jobEvent);
        log.info("Published JOB_CREATED event for job: {}", saved.getId());


        return jobMapper.toJobResponse(saved);
    }

    public List<JobResponse> getAll() {
        return jobRepository.findAllByIsDeletedFalse().stream()
                .map(jobMapper::toJobResponse)
                .toList();
    }


//    public List<String> getJobType() {
//        List<Job> listAll = jobRepository.findAll();
//
//        Set<String> uniqueJobTypes = new LinkedHashSet<>();
//
//        for (Job job : listAll) {
//            if (job.getJobType() != null) {
//                uniqueJobTypes.add(String.valueOf(job.getJobType()));
//            }
//        }
//
//
//        return new ArrayList<>(uniqueJobTypes);
//    }
//
//
//    public List<String> getCategory() {
//        List<Job> listAll = jobRepository.findAll();
//
//        Set<String> uniqueJobTypes = new LinkedHashSet<>();
//
//        for (Job job : listAll) {
//            if (job.getCategory() != null) {
//                uniqueJobTypes.add(String.valueOf(job.getCategory()));
//            }
//        }
//
//
//        return new ArrayList<>(uniqueJobTypes);
//    }
//
//
//    public List<String> getExperience() {
//        List<Job> listAll = jobRepository.findAll();
//
//        Set<String> uniqueJobTypes = new LinkedHashSet<>();
//
//        for (Job job : listAll) {
//            if (job.getExperience() != null) {
//                uniqueJobTypes.add(String.valueOf(job.getExperience()));
//            }
//        }
//
//
//        return new ArrayList<>(uniqueJobTypes);
//    }


    public List<String> getUniqueField(Function<Job, Object> fieldExtractor) {
        List<Job> listAll = jobRepository.findAllByIsDeletedFalse();

        Set<String> uniqueValues = new LinkedHashSet<>();

        for (Job job : listAll) {
            Object value = fieldExtractor.apply(job);

            if (value != null) {
                uniqueValues.add(String.valueOf(value));
            }
        }

        return new ArrayList<>(uniqueValues);
    }

    public List<String> getJobType() {
        return getUniqueField(Job::getJobType);
    }

    public List<String> getCategory() {
        return getUniqueField(Job::getCategory);
    }

    public List<String> getExperience() {
        return getUniqueField(Job::getExperience);
    }
//
//    public List<String> getCompanyName()
//    {
//        return getUniqueField(Job::getCompanyName);
//    }

    public List<String> getTitle() {
        return getUniqueField(Job::getTitle);
    }

    public List<String> getLocation() {
        return getUniqueField(Job::getLocation);
    }

    public List<String> getSalary() {
        return getUniqueField(Job::getSalary);
    }

    public Page<JobResponse> getByUser(int page, int size, Long userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Job> jobs = jobRepository.findByUserIdAndIsDeletedFalse(userId, pageable);
        return jobs.map(jobMapper::toJobResponse);
    }


    public List<String> getJobTypesDefault() {
        return Arrays.stream(JobType.values())
                .map(Enum::name)
                .toList();
    }

    public Page<JobResponse> getByCompanyId(int page, int size, Long userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Job> jobs = jobRepository.findByUserId(userId, pageable);
        return jobs.map(jobMapper::toJobResponse);
    }


    public long countJobsByCompany(Long companyId) {
        return jobRepository.countByCompanyIdAndIsDeletedFalse(companyId);
    }


    public Page<JobResponse> getJobPaged(int page, int size, String keyword, String location, String experience, String category, String jobType, Long companyId, Long userId) {


        Pageable pageable = PageRequest.of(page, size);
        Specification<Job> spec = JobSpecification.filter(keyword, location, experience, category, jobType, companyId, userId);
        Page<Job> jobs = jobRepository.findAll(spec, pageable);

        // return jobs.map(jobMapper::toJobResponse);


//        return jobs.map( job -> {
//
//            ApiResponse<CompanyResponse> companyResponse = getCompanyName(job.getCompanyId());
//
//            JobResponse response=jobMapper.toJobResponse(job);
//            response.setCompanyName(companyResponse.getResult().getCompanyName());
//
//
//            return response;
//        });


        return jobs.map(job -> {
            JobResponse response = jobMapper.toJobResponse(job);

            ApiResponse<CompanyResponse> companyResp = getCompanyName(job.getCompanyId());
            response.setCompanyName(companyResp.getResult() != null ?
                    companyResp.getResult().getName() : companyResp.getMessage());
            response.setCompanyLogo(companyResp.getResult() != null ?
                    companyResp.getResult().getLogo() : companyResp.getMessage());

            return response;
        });

    }


    public JobResponse getById(Long jobId) {

        Job job = jobRepository.findByIdAndIsDeletedFalse(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_UNAVAILABLE));


        var authentication = SecurityContextHolder.getContext().getAuthentication();


        boolean isCompanyOrAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_COMPANY") || a.getAuthority().equals("ROLE_ADMIN"));

        if (!isCompanyOrAdmin) {
            job.setViews(job.getViews() + 1);
            jobRepository.save(job);
        }

        // return jobMapper.toJobResponse(job);

        // Map to JobResponse
        JobResponse response = jobMapper.toJobResponse(job);

        // get information company
        ApiResponse<CompanyResponse> companyResp = getCompanyName(job.getCompanyId());
        CompanyResponse company = companyResp.getResult();

        response.setCompanyName(company != null ? company.getName() : "Unknown");
        response.setCompanyLogo(company != null ? company.getLogo() : null);

        return response;


    }


//    public ApiResponse<CompanyResponse> getCompanyName(Long companyId) {
//        try {
//            String token = httpServletRequest.getHeader("Authorization");
//            return companyClient.getCompanyNameById(token, companyId);
//        } catch (FeignException.NotFound e) {
//            throw new AppException(ErrorCode.COMPANY_NOT_FOUND);
//        } catch (Exception e) {
//            throw new AppException(ErrorCode.COMPANY_SERVICE_ERROR);  // Service lỗi
//        }
//    }


    public ApiResponse<CompanyResponse> getCompanyName(Long companyId) {
        if (companyId == null || companyId <= 0) {
            throw new AppException(ErrorCode.COMPANY_NOT_FOUND);
        }

        try {
            String token = httpServletRequest.getHeader("Authorization");
            return companyClient.getCompanyNameById(token, companyId);
        } catch (FeignException.NotFound e) {
            throw new AppException(ErrorCode.COMPANY_NOT_FOUND);
        } catch (Exception e) {
            throw new AppException(ErrorCode.COMPANY_SERVICE_ERROR);
        }
    }


    public List<JobResponse> getRelatedJobs(Long jobId) {
        Job currentJob = jobRepository.findByIdAndIsDeletedFalse(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        List<Job> allJobs = jobRepository.findAll();

        List<JobResponse> relatedJobs = new ArrayList<>();

        for (Job job : allJobs) {
            if (currentJob.getCategory().equals(job.getCategory())
                    && !currentJob.getId().equals(job.getId())
                    && currentJob.getCompanyId().equals(job.getCompanyId())) {

                // Convert job sang response
                JobResponse response = jobMapper.toJobResponse(job);

                try {
                    ApiResponse<CompanyResponse> companyResp = getCompanyName(job.getCompanyId());
                    if (companyResp != null && companyResp.getResult() != null) {
                        response.setCompanyName(companyResp.getResult().getName());
                        response.setCompanyLogo(companyResp.getResult().getLogo());
                    } else {
                        response.setCompanyName("Unknown Company");
                    }
                } catch (Exception e) {
                    response.setCompanyName("Unknown Company");
                }

                relatedJobs.add(response);
            }

        }
        return relatedJobs;

    }


    public boolean checkApply(Long jobId) {
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());

        return jobRepository.existsByUserIdAndId(userId, jobId);
    }


    public JobResponse updateJob(Long id, JobUpdateRequest request) {
//        var authentication = SecurityContextHolder.getContext().getAuthentication();
//        Long userIds = Long.valueOf(authentication.getName());


        var job = jobRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        if (request.getExpiredAt() != null && request.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.EXPIRED_DATE_INVALID);
        }


        Optional.ofNullable(request.getTitle())
                .ifPresent(job::setTitle);

        Optional.ofNullable(request.getDescription())
                .ifPresent(job::setDescription);

        Optional.ofNullable(request.getSalary())
                .ifPresent(job::setSalary);

        Optional.ofNullable(request.getLocation())
                .ifPresent(job::setLocation);

        Optional.ofNullable(request.getExperience())
                .ifPresent(job::setExperience);

        Optional.ofNullable(request.getJobType())
                .ifPresent(job::setJobType);

        Optional.ofNullable(request.getCategory())
                .ifPresent(job::setCategory);

        Optional.ofNullable(request.getExpiredAt())
                .ifPresent(job::setExpiredAt);

        var saved = jobRepository.save(job);
        return jobMapper.toJobResponse(saved);
    }


    public Page<JobResponse> getActiveJobs(Pageable pageable, String keyword) {
        Page<Job> page;
        if (keyword != null && !keyword.isEmpty()) {
            page = jobRepository.findByIsDeletedFalseAndTitleContainingIgnoreCase(keyword, pageable);
        } else {
            page = jobRepository.findByIsDeletedFalse(pageable);
        }
        return page.map(jobMapper::toJobResponse);
    }


    public Page<JobResponse> getDeletedJobs(Pageable pageable, String keyword)
    {
        Page<Job> page;
        if (keyword != null && !keyword.isEmpty()) {
            page = jobRepository.findByIsDeletedTrueAndTitleContainingIgnoreCase(keyword, pageable);
        } else {
            page = jobRepository.findByIsDeletedTrue(pageable);
        }
        return page.map(jobMapper::toJobResponse);
    }


    public void restoreJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
        job.setIsDeleted(false);
        jobRepository.save(job);
    }


    //soft delete
    public void deleteJob(Long id) {
//        var authentication = SecurityContextHolder.getContext().getAuthentication();
//        Long userId = Long.valueOf(authentication.getName());

        var job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

//        // Lấy token từ request hiện tại
//        String token = httpServletRequest.getHeader("Authorization");
//
//        System.out.println("=== TOKEN from request: " + token);
//        System.out.println("=== TOKEN length: " + (token != null ? token.length() : 0));
//
//        if (token != null && !token.startsWith("Bearer ")) {
//            token = "Bearer " + token;
//            System.out.println("=== Fixed token: " + token);
//        }
//
//        try {
//            ApiResponse<Long> response = applicationClient.countPendingApplicationsByJob(id, userId, token);
//            Long pendingCount = response.getResult();
//
//            if (pendingCount != null && pendingCount > 0) {
//                throw new AppException(ErrorCode.CANNOT_DELETE_JOB_WITH_PENDING_APPLICATIONS);
//            }
//        } catch (FeignException e) {
//            System.out.println("=== Feign error status: " + e.status());
//            System.out.println("=== Feign error content: " + e.contentUTF8());
//            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
//        }

        job.setIsDeleted(true);
        jobRepository.save(job);


// Publish job deleted event
        JobEvent jobEvent = new JobEvent();
        jobEvent.setEventId(UUID.randomUUID().toString());
        jobEvent.setEventType("JOB_SOFT_DELETED");
        jobEvent.setSource("job-service");
        jobEvent.setJobId(id);
        jobEvent.setCompanyId(job.getCompanyId());
        jobEvent.setAction("SOFT_DELETED");
        jobEvent.setTitle(job.getTitle());
        jobEvent.setCategory(job.getCategory());

        kafkaTemplate.send(JobEvent.TOPIC, jobEvent);
        log.info("Published JOB_SOFT_DELETED event for job: {}", id);

    }


    public void hardDeleteJob(Long id) {
        var job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        String token = httpServletRequest.getHeader("Authorization");
        // xóa application trước
        try {
            applicationClient.deleteApplicationsByJobId(id,token);
            log.info("Deleted all applications for job {}", id);
        } catch (Exception e) {
            log.error("Failed to delete applications for job {}", id, e);
            throw new AppException(ErrorCode.CANNOT_DELETE_JOB);
        }

        // xóa job
        jobRepository.deleteById(job.getId());
        log.info("Job {} has been hard deleted by admin", id);

//        JobEvent jobEvent = new JobEvent();
//        jobEvent.setEventId(UUID.randomUUID().toString());
//        jobEvent.setEventType("JOB_HARD_DELETED");
//        jobEvent.setSource("job-service");
//        jobEvent.setJobId(id);
//        jobEvent.setCompanyId(job.getCompanyId());
//        jobEvent.setAction("HARD_DELETED");
//        kafkaTemplate.send(JobEvent.TOPIC, jobEvent);
//        log.info("Published JOB_HARD_DELETED event for job: {}", id);


    }


    public void updateCompanyInfoForJobs(Long companyId, String newName, String newLogo) {
        List<Job> jobs = jobRepository.findByCompanyIdAndIsDeletedFalse(companyId);
        for (Job job : jobs) {
            job.setCompanyName(newName);
            job.setCompanyLogo(newLogo);
        }
        jobRepository.saveAll(jobs);
    }


    // Listen to company events
//    @KafkaListener(topics = CompanyEvent.TOPIC, groupId = "job-service-group")
//    public void handleCompanyEvent(CompanyEvent event)
//    {
//        log.info("Received company event: {} for company: {}", event.getAction(), event.getCompanyId());
//
//        switch (event.getAction())
//        {
//            case "SOFT_DELETED":
//            case "HARD_DELETED":
//
//                // Soft delete all jobs of this company
//                List<Job> jobs = jobRepository.findByCompanyIdAndIsDeletedFalse(event.getCompanyId());
//                if ("SOFT_DELETED".equals(event.getAction()))
//                {
//                    jobs.forEach(job ->
//                    {
//                        job.setIsDeleted(true);
//                        jobRepository.save(job);
//                        log.info("Soft deleted job {} for deleted company {}", job.getId(), event.getCompanyId());
//                    });
//                }
//                else
//                {
//                    // HARD_DELETED
//                    jobRepository.deleteAll(jobs);
//                    log.info("Hard deleted {} jobs for deleted company {}", jobs.size(), event.getCompanyId());
//                }
//                break;
//
//            case "UPDATED":
//                log.info("Company {} updated, updating category for all jobs...", event.getCompanyId());
//
//                List<Job> jobsToUpdate = jobRepository.findByCompanyIdAndIsDeletedFalse(event.getCompanyId());
//
//                if (!jobsToUpdate.isEmpty()) {
//                    jobsToUpdate.forEach(job -> {
//                        job.setCategory(event.getCategory());
//                        jobRepository.save(job);
//                    });
//                    log.info("Updated category for {} jobs of company {}", jobsToUpdate.size(), event.getCompanyId());
//                } else {
//                    log.info("No active jobs found for company {}", event.getCompanyId());
//                }
//                break;
//
//            case "CREATED":
//                log.info("New company created: {} (ID: {})", event.getCompanyName(), event.getCompanyId());
//                break;
//
//            default:
//                log.debug("Unknown action: {}", event.getAction());
//        }
//
//    }






    @KafkaListener(topics = CompanyEvent.TOPIC, groupId = "job-service-group")
    public void handleCompanyEvent(CompanyEvent event)
    {
        log.info("Company event: {} - {}", event.getAction(), event.getCompanyId());

        switch (event.getAction())
        {
            case "SOFT_DELETED":
            case "HARD_DELETED":
                jobRepository.findByCompanyIdAndIsDeletedFalse(event.getCompanyId())
                        .forEach(job ->
                        {
                            if ("SOFT_DELETED".equals(event.getAction()))
                                {deleteJob(job.getId());}

                            else hardDeleteJob(job.getId());
                        });
                break;

            case "UPDATED":
                updateCompanyInfoForJobs(event.getCompanyId(),event.getCompanyName(),event.getLogo());
                break;
            default:
                log.debug("No action needed for job event: {}", event.getAction());
        }
    }


}