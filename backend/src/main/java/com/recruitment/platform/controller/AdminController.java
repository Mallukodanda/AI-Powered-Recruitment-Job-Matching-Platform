package com.recruitment.platform.controller;

import com.recruitment.platform.dto.UserResponse;
import com.recruitment.platform.model.AuditLog;
import com.recruitment.platform.model.Job;
import com.recruitment.platform.model.PlatformConfig;
import com.recruitment.platform.model.Role;
import com.recruitment.platform.service.AdminService;
import com.recruitment.platform.service.AuditService;
import com.recruitment.platform.service.PlatformConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Administration", description = "User Management, Role Assignments, System Observability, and Audit Trails")
public class AdminController {

    private final AdminService adminService;
    private final AuditService auditService;
    private final PlatformConfigService configService;

    public AdminController(AdminService adminService,
                           AuditService auditService,
                           PlatformConfigService configService) {
        this.adminService = adminService;
        this.auditService = auditService;
        this.configService = configService;
    }

    // =========================================================================
    // USER MANAGEMENT
    // =========================================================================

    @GetMapping("/users")
    @Operation(summary = "List all platform users with role filtering, search, and pagination")
    public ResponseEntity<Page<UserResponse>> listUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Principal principal) {
        String caller = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(adminService.listUsers(role, search, pageable, caller));
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Update user role (e.g. promote to RECRUITER or ADMIN)")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            Principal principal) {
        String caller = principal != null ? principal.getName() : null;
        Role newRole = Role.valueOf(payload.get("role").toUpperCase());
        return ResponseEntity.ok(adminService.updateUserRole(id, newRole, caller));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Toggle user active state (activate / deactivate account)")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload,
            Principal principal) {
        String caller = principal != null ? principal.getName() : null;
        Boolean active = payload.get("active");
        if (active == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(adminService.updateUserStatus(id, active, caller));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user account")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            Principal principal) {
        String caller = principal != null ? principal.getName() : null;
        adminService.deleteUser(id, caller);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // JOB OVERSIGHT
    // =========================================================================

    @GetMapping("/jobs")
    @Operation(summary = "Oversee all posted job requisitions across all companies")
    public ResponseEntity<Page<Job>> listAllJobs(
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Principal principal) {
        String caller = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(adminService.listAllJobs(pageable, caller));
    }

    @PatchMapping("/jobs/{id}/status")
    @Operation(summary = "Update job requisition status (ACTIVE, PAUSED, CLOSED, ARCHIVED)")
    public ResponseEntity<Job> updateJobStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            Principal principal) {
        String caller = principal != null ? principal.getName() : null;
        String status = payload.get("status");
        return ResponseEntity.ok(adminService.updateJobStatus(id, status, caller));
    }

    // =========================================================================
    // AUDIT LOGS & PLATFORM CONFIGS
    // =========================================================================

    @GetMapping("/audit-logs")
    @Operation(summary = "View security and operational audit trail with filters and pagination")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable,
            Principal principal) {
        String caller = principal != null ? principal.getName() : null;
        adminService.verifyAdminAccess(caller);
        return ResponseEntity.ok(auditService.getAuditLogs(actor, action, resourceType, pageable));
    }

    @GetMapping("/configs")
    @Operation(summary = "List all runtime dynamic platform configurations")
    public ResponseEntity<List<PlatformConfig>> getConfigs(Principal principal) {
        String caller = principal != null ? principal.getName() : null;
        adminService.verifyAdminAccess(caller);
        return ResponseEntity.ok(configService.getAllConfigs());
    }

    @PatchMapping("/configs/{key}")
    @Operation(summary = "Update dynamic platform configuration threshold or toggle")
    public ResponseEntity<PlatformConfig> updateConfig(
            @PathVariable String key,
            @RequestBody Map<String, String> payload,
            Principal principal) {
        String caller = principal != null ? principal.getName() : null;
        adminService.verifyAdminAccess(caller);
        String value = payload.get("value");
        return ResponseEntity.ok(configService.updateConfig(key, value, caller));
    }

    @GetMapping("/reports/summary")
    @Operation(summary = "Executive platform overview report")
    public ResponseEntity<Map<String, Object>> getReport(Principal principal) {
        String caller = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(adminService.getRecruitmentReport(caller));
    }
}
