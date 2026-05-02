package com.meterflow.repository;

import com.meterflow.entity.ApiKey;
import com.meterflow.entity.ApiKeyStatus;
import com.meterflow.entity.ApiProject;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends MongoRepository<ApiKey, String> {
    List<ApiKey> findByApi(ApiProject api);
    List<ApiKey> findByApiIn(Collection<ApiProject> apis);
    Optional<ApiKey> findByKeyValueAndStatus(String keyValue, ApiKeyStatus status);
}
