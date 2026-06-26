package com.tihuz.common.event;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public abstract class BaseEvent
{
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String source;

    public BaseEvent() {
        this.timestamp = LocalDateTime.now();
    }
}