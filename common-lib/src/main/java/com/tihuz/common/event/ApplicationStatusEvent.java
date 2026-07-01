package com.tihuz.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusEvent
{
    public static final String TOPIC = "application-status-events";

    private Long applicationId;
    private Long userId;
    private String userEmail;
    private String userUsername;
    private String jobTitle;
    private String newStatus;   // ACCEPTED or REJECTED
    private String oldStatus;   // PENDING (optional)
}