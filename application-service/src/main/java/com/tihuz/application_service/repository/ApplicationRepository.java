package com.tihuz.application_service.repository;


import com.tihuz.application_service.Enum.ApplicationsStatus;
import com.tihuz.application_service.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Page<Application> findByUserId(Long userId, Pageable pageable);

    List<Application> findByJobId(Long jobId);

    Page<Application> findByJobId(Long jobId, Pageable pageable);

    boolean existsByUserIdAndJobId(Long userId, Long Id);

    //SELECT * FROM application WHERE job_id IN (?, ?, ?)
    Page<Application> findByJobIdIn(List<Long> jobIds, Pageable pageable);

    long countByJobId(Long jobId);

    long countByJobIdAndStatus(Long jobId, ApplicationsStatus status);

    long countByJobIdIn(List<Long> jobIds);


    // Chỉ lấy application chưa bị xóa
    List<Application> findByJobIdAndIsDeletedFalse(Long jobId);

    Page<Application> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);
    Page<Application> findByJobIdAndIsDeletedFalse(Long jobId,Pageable pageable);
    Page<Application> findByJobIdInAndIsDeletedFalse(List<Long> jobIds, Pageable pageable);
    long countByJobIdAndStatusAndIsDeletedFalse(Long jobId, ApplicationsStatus status);
    long countByJobIdAndIsDeletedFalse(Long jobId);
    long countByJobIdInAndIsDeletedFalse(List<Long> jobIds);


    Page<Application> findByIsDeletedFalse(Pageable pageable);
    Page<Application> findByIsDeletedFalseAndJobId(Long jobId, Pageable pageable);
    Page<Application> findByIsDeletedFalseAndUserId(Long userId, Pageable pageable);
    Page<Application> findByIsDeletedTrue(Pageable pageable);
    Page<Application> findByIsDeletedTrueAndJobId(Long jobId, Pageable pageable);
    Page<Application> findByIsDeletedTrueAndUserId(Long userId, Pageable pageable);
}