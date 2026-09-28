package com.recruitment.platform.repository;

import com.recruitment.platform.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    Page<Company> findByVerified(boolean verified, Pageable pageable);

    Page<Company> findByIndustryIgnoreCase(String industry, Pageable pageable);
}
