package com.Codexa.Codexa.controller;

import com.Codexa.Codexa.dto.DashboardResponse;
import com.Codexa.Codexa.service.DashboardService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService) {

        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            Authentication authentication) {

        String email =
                authentication.getName();

        return ResponseEntity.ok(
                dashboardService.getDashboard(
                        email
                )
        );
    }
}