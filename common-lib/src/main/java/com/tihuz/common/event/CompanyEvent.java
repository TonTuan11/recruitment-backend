package com.tihuz.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor

public class CompanyEvent extends BaseEvent {
    public static final String TOPIC = "company-events";

    private Long companyId;
    private String action; // CREATED, UPDATED, SOFT_DELETED, HARD_DELETED

    // Những field cần đồng bộ khi có UPDATE
    private String companyName;
    private String logo;
    private String website;
    private String email;
    private String scale;
    private String description;
    private String address;
    private String category;
}