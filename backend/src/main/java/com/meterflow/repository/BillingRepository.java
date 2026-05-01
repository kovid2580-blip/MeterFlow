package com.meterflow.repository;

import com.meterflow.entity.Billing;
import com.meterflow.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BillingRepository extends MongoRepository<Billing, String> {
    List<Billing> findByUserOrderByMonthDesc(User user);
    Optional<Billing> findByUserAndMonth(User user, String month);
}
