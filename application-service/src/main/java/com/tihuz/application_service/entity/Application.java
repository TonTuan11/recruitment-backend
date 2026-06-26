package com.tihuz.application_service.entity;

import com.tihuz.application_service.Enum.ApplicationsStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "applications")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long userId;
    String userName;

    @Column(nullable = false)
    Long jobId;
    String jobTitle;


    //+ Snapshot by Json
    @Column(columnDefinition = "TEXT")
    String jobSnapshotJson;

    String cvUrl;

    @Column(columnDefinition = "TEXT")
    String coverLetter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ApplicationsStatus status;

    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;

    @Builder.Default
    Boolean isDeleted = false;
}