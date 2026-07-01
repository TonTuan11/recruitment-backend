package com.tihuz.common.event;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseEvent
{
     String eventId;
     String eventType;
     LocalDateTime timestamp;
     String source;

    public BaseEvent() {
        this.timestamp = LocalDateTime.now();
    }
}
