package com.tihuz.common.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewApplicationEvent
{
    public static final String TOPIC = "new-application-events";

     Long applicationId;
     Long jobId;
     String jobTitle;
     Long companyId;
     String companyName;
     String companyEmail;
     String companyUserName;
     Long userId;
     String applicantName;
     String applicantEmail;
     String cvUrl;
     String coverLetter;
}