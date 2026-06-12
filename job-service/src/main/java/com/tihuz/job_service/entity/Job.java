package com.tihuz.job_service.entity;

import com.tihuz.job_service.Enum.JobType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "jobs")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long userId;

    @Column(nullable = false)
    Long companyId;


    @Column(nullable = false)
    String companyName;

    String companyLogo;

    @Column(nullable = false)
    String title;

    @Column(columnDefinition = "TEXT")
    String description;

    String salary;

    String location;

    String experience;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    JobType jobType;

    String category;

    LocalDateTime expiredAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;

    @Builder.Default
     Boolean isDeleted = false;


     long views;

}
