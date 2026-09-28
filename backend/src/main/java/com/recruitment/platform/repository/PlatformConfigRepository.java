package com.recruitment.platform.repository;

import com.recruitment.platform.model.PlatformConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformConfigRepository extends JpaRepository<PlatformConfig, Long> {

    Optional<PlatformConfig> findByConfigKey(String configKey);

    boolean existsByConfigKey(String configKey);

    List<PlatformConfig> findByCategory(String category);
}
