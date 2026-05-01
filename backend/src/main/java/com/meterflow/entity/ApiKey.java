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
@Document(collection = "api_keys")
public class ApiKey {
    @Id
    
    private String id;

    @DBRef
    
    private ApiProject api;

    @Indexed(unique = true)
    private String keyValue;

    
    
    private ApiKeyStatus status;

    
    
    private PlanType planType;

    
    private Instant createdAt;

    
    void prePersist() {
        createdAt = Instant.now();
    }
}
