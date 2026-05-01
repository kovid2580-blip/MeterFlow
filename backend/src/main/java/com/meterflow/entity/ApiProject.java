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
@Document(collection = "apis")
public class ApiProject {
    @Id
    
    private String id;

    @DBRef
    
    private User user;

    
    private String name;

    
    private String baseUrl;

    
    private String description;

    
    private Instant createdAt;

    
    void prePersist() {
        createdAt = Instant.now();
    }
}
