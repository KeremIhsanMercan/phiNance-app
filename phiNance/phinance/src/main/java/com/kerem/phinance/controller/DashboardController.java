package com.kerem.phinance.controller;

import com.kerem.phinance.dto.DashboardDto;
import com.kerem.phinance.security.UserPrincipal;
import com.kerem.phinance.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard and analytics APIs")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Get dashboard data")
    public ResponseEntity<DashboardDto> getDashboard(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(dashboardService.getDashboard(userPrincipal.getId()));
    }
}
