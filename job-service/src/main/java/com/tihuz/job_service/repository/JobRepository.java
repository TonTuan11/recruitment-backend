package com.tihuz.job_service.repository;

import com.tihuz.job_service.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job,Long>, JpaSpecificationExecutor<Job> {


    Page<Job> findByUserId(Long userId, Pageable pageable);
   // Page<Post> findByContentContainingIgnoreCase(String keyword, Pageable pageable);
    List<Job> findByUserId(Long userId);
    List<Job> findByCompanyId(Long companyId);
    boolean existsByUserIdAndId(Long userId, Long jobId);
    long countByCompanyId(Long companyId);




    Optional<Job> findByIdAndIsDeletedFalse(Long id);
    Page<Job> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);
    List<Job> findByCompanyIdAndIsDeletedFalse(Long companyId);
    Page<Job> findAllByIsDeletedFalse(Pageable pageable);
    List<Job> findAllByIsDeletedFalse();
    long countByCompanyIdAndIsDeletedFalse(Long companyId);

    Page<Job> findByIsDeletedFalse(Pageable pageable);
    Page<Job> findByIsDeletedFalseAndTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Job> findByIsDeletedTrue(Pageable pageable);
    Page<Job> findByIsDeletedTrueAndTitleContainingIgnoreCase(String title, Pageable pageable);
}
