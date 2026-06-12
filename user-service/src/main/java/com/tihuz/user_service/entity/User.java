package com.tihuz.user_service.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tihuz.user_service.Enum.RoleType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false,  unique = true)
    String username;

    Long companyId;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false)
    String password;

    String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    RoleType role;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    LocalDateTime updatedAt;

    @Builder.Default
    Boolean isDeleted = false;
}
