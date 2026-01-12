package com.digital.magazine.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.admin.dto.DashboardStatsDto;
import com.digital.magazine.admin.service.AdminDashboardService;
import com.digital.magazine.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

	private final AdminDashboardService dashboardService;

	@GetMapping("/stats")
	public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats() {

		return ResponseEntity.ok(new ApiResponse<>(dashboardService.getDashboardStats()));
	}
}
