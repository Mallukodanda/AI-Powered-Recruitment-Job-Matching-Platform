package com.recruitment.platform.controller;

import com.recruitment.platform.model.Company;
import com.recruitment.platform.service.AdminService;
import com.recruitment.platform.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/companies")
@Tag(name = "Companies", description = "Employer Organizations, Verification, and Profile Management")
public class CompanyController {

    private final CompanyService companyService;
    private final AdminService adminService;

    public CompanyController(CompanyService companyService, AdminService adminService) {
        this.companyService = companyService;
        this.adminService = adminService;
    }

    @PostMapping
    @Operation(summary = "Register a new company organization")
    public ResponseEntity<Company> createCompany(
            @RequestBody Company company,
            Principal principal) {
        String caller = principal != null ? principal.getName() : "ANONYMOUS";
        return ResponseEntity.status(HttpStatus.CREATED).body(companyService.createCompany(company, caller));
    }

    @GetMapping
    @Operation(summary = "List registered companies with pagination")
    public ResponseEntity<Page<Company>> listCompanies(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(companyService.getAllCompanies(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get company details by ID")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update company profile information")
    public ResponseEntity<Company> updateCompany(
            @PathVariable Long id,
            @RequestBody Company updateData,
            Principal principal) {
        String caller = principal != null ? principal.getName() : "ANONYMOUS";
        return ResponseEntity.ok(companyService.updateCompany(id, updateData, caller));
    }

    @PatchMapping("/{id}/verify")
    @Operation(summary = "Toggle company verified status (Administrator only)")
    public ResponseEntity<Company> verifyCompany(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload,
            Principal principal) {
        String caller = principal != null ? principal.getName() : null;
        adminService.verifyAdminAccess(caller);
        boolean verified = payload.getOrDefault("verified", true);
        return ResponseEntity.ok(companyService.verifyCompany(id, verified, caller));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete company organization (Administrator only)")
    public ResponseEntity<Void> deleteCompany(
            @PathVariable Long id,
            Principal principal) {
        String caller = principal != null ? principal.getName() : null;
        adminService.verifyAdminAccess(caller);
        companyService.deleteCompany(id, caller);
        return ResponseEntity.noContent().build();
    }
}
