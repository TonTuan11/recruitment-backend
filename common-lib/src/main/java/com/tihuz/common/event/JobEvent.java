package com.tihuz.common.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor

public class JobEvent extends BaseEvent {
    public static final String TOPIC = "job-events";

    private Long jobId;
    private Long companyId;
    private String action; // CREATED, UPDATED, SOFT_DELETED, HARD_DELETED

    // Những field cần đồng bộ khi có UPDATE
    private String title;
    private String description;
    private String salary;
    private String location;
    private String experience;
    private String  jobType;
    private String category;
    private LocalDateTime expiredAt;
}