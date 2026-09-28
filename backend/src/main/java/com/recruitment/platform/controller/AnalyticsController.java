package com.recruitment.platform.controller;

import com.recruitment.platform.dto.DashboardStatsDto;
import com.recruitment.platform.service.ApplicationWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics & KPIs", description = "Recruitment Metrics & Performance Dashboards")
public class AnalyticsController {

    private final ApplicationWorkflowService workflowService;

    public AnalyticsController(ApplicationWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get high-level recruitment analytics and executive KPIs")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(workflowService.getDashboardStats());
    }
}
