package com.meterflow.entity;

import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.*;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "billing")
public class Billing {
    @Id
    
    private String id;

    @DBRef
    
    private User user;

    
    private String month;

    
    private long totalRequests;

    
    private BigDecimal amount;

    
    
    private PaymentStatus paymentStatus;
}
