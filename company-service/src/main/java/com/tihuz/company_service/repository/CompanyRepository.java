package com.tihuz.company_service.repository;

import com.tihuz.company_service.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String name);
    boolean existsByName(String name);

    List<Company> findByNameContainingIgnoreCase(String name);


    Optional<Company> findByIdAndIsDeletedFalse(Long id);
    Page<Company> findByIsDeletedFalse(Pageable pageable);
    Optional<Company> findByNameAndIsDeletedFalse(String name);
    List<Company> findAllByIsDeletedFalse();
    List<Company> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name);


    Page<Company> findByIsDeletedFalseAndNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Company> findByIsDeletedTrue(Pageable pageable);
    Page<Company> findByIsDeletedTrueAndNameContainingIgnoreCase(String name, Pageable pageable);

}