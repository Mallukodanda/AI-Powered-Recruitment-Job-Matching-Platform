package com.recruitment.platform.service;

import com.recruitment.platform.exception.ResourceNotFoundException;
import com.recruitment.platform.model.Company;
import com.recruitment.platform.repository.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final AuditService auditService;

    public CompanyService(CompanyRepository companyRepository, AuditService auditService) {
        this.companyRepository = companyRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Company createCompany(Company company, String actorEmail) {
        if (company.getName() == null || company.getName().isBlank()) {
            throw new IllegalArgumentException("Company name is required.");
        }
        if (companyRepository.existsByNameIgnoreCase(company.getName().trim())) {
            throw new IllegalArgumentException("Company already exists with name: " + company.getName());
        }

        Company saved = companyRepository.save(company);
        auditService.logEvent(
                actorEmail,
                "COMPANY_CREATED",
                "COMPANY",
                saved.getId().toString(),
                "SUCCESS",
                "Created company: " + saved.getName(),
                null
        );
        return saved;
    }

    @Transactional
    public Company updateCompany(Long id, Company updateData, String actorEmail) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));

        if (updateData.getName() != null && !updateData.getName().isBlank()) {
            company.setName(updateData.getName().trim());
        }
        if (updateData.getIndustry() != null) {
            company.setIndustry(updateData.getIndustry().trim());
        }
        if (updateData.getWebsite() != null) {
            company.setWebsite(updateData.getWebsite().trim());
        }
        if (updateData.getLocation() != null) {
            company.setLocation(updateData.getLocation().trim());
        }
        if (updateData.getContactEmail() != null) {
            company.setContactEmail(updateData.getContactEmail().trim());
        }
        if (updateData.getDescription() != null) {
            company.setDescription(updateData.getDescription());
        }

        Company saved = companyRepository.save(company);
        auditService.logEvent(
                actorEmail,
                "COMPANY_UPDATED",
                "COMPANY",
                saved.getId().toString(),
                "SUCCESS",
                "Updated company: " + saved.getName(),
                null
        );
        return saved;
    }

    @Transactional
    public Company verifyCompany(Long id, boolean verified, String actorEmail) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));

        company.setVerified(verified);
        Company saved = companyRepository.save(company);
        auditService.logEvent(
                actorEmail,
                verified ? "COMPANY_VERIFIED" : "COMPANY_UNVERIFIED",
                "COMPANY",
                saved.getId().toString(),
                "SUCCESS",
                "Company verification set to: " + verified,
                null
        );
        return saved;
    }

    @Transactional(readOnly = true)
    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Company> getAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    @Transactional
    public void deleteCompany(Long id, String actorEmail) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));

        companyRepository.delete(company);
        auditService.logEvent(
                actorEmail,
                "COMPANY_DELETED",
                "COMPANY",
                id.toString(),
                "SUCCESS",
                "Deleted company: " + company.getName(),
                null
        );
    }
}
