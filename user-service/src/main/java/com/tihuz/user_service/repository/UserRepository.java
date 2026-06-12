package com.tihuz.user_service.repository;

import com.tihuz.user_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findById(Long id);
    Optional<User> findByUsername(String name);
    boolean existsByUsername(String name);
    boolean existsByEmail (String email);


    Optional<User> findByIdAndIsDeletedFalse(Long id);
    List<User> findAllByIsDeletedFalse();
    Page<User> findAllByIsDeletedFalse(Pageable pageable);
    Optional<User> findByUsernameAndIsDeletedFalse(String username);
    Optional<User> findByEmailAndIsDeletedFalse(String email);



    // Active users (isDeleted = false)
    Page<User> findByIsDeletedFalse(Pageable pageable);
    Page<User> findByIsDeletedFalseAndUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String email, Pageable pageable);

    // Deleted users (isDeleted = true)
    Page<User> findByIsDeletedTrue(Pageable pageable);
    Page<User> findByIsDeletedTrueAndUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String email, Pageable pageable);

}
