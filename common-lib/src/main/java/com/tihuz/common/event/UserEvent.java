// common-lib/src/main/java/com/tihuz/common/event/UserEvent.java
package com.tihuz.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor

public class UserEvent extends BaseEvent {
    public static final String TOPIC = "user-events";

    private Long userId;
    private Long companyId;
    private String username;
    private String email;
    private String action; // CREATED, UPDATED, DELETED, COMPANY_LINKED
    private String role;
}