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
    private final com.recruitment.platform.service.PlatformAnalyticsService platformAnalyticsService;

    public AnalyticsController(ApplicationWorkflowService workflowService,
                               com.recruitment.platform.service.PlatformAnalyticsService platformAnalyticsService) {
        this.workflowService = workflowService;
        this.platformAnalyticsService = platformAnalyticsService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get high-level recruitment analytics and executive KPIs")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(workflowService.getDashboardStats());
    }

    @GetMapping("/platform")
    @Operation(summary = "Get comprehensive platform-wide metrics with database aggregations")
    public ResponseEntity<com.recruitment.platform.dto.PlatformAnalyticsDto> getPlatformAnalytics() {
        return ResponseEntity.ok(platformAnalyticsService.getPlatformAnalytics());
    }
}
