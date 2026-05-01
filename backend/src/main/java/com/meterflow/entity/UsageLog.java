package com.meterflow.entity;

import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.*;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "usage_logs")
public class UsageLog {
    @Id
    
    private String id;

    
    private String apiKey;

    
    private String endpoint;

    
    private String method;

    
    private Instant timestamp;

    
    private int statusCode;

    
    private long latencyMs;
}
