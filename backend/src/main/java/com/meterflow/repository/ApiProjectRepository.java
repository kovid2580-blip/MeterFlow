package com.meterflow.repository;

import com.meterflow.entity.ApiProject;
import com.meterflow.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ApiProjectRepository extends MongoRepository<ApiProject, String> {
    List<ApiProject> findByUser(User user);
    Optional<ApiProject> findByName(String name);
}
