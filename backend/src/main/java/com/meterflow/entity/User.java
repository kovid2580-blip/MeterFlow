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
@Document(collection = "users")
public class User {
    @Id
    
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    
    private String password;

    
    
    private Role role;

    @Builder.Default
    private PlanType planType = PlanType.FREE;

    private String razorpaySubscriptionId;

    private String subscriptionStatus;

    private Instant subscriptionVerifiedAt;
}
