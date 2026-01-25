package com.digital.magazine.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.admin.dto.DashboardStatsDto;
import com.digital.magazine.admin.dto.SubscriptionStatsResponse;
import com.digital.magazine.admin.service.AdminDashboardService;
import com.digital.magazine.common.response.ApiResponse;
import com.digital.magazine.payment.dto.PaymentSummaryDto;
import com.digital.magazine.payment.service.PaymentStatsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

	private final AdminDashboardService dashboardService;
	private final PaymentStatsService service;

	@GetMapping("/stats")
	public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats() {

		return ResponseEntity.ok(new ApiResponse<>(dashboardService.getDashboardStats()));
	}

	@GetMapping("/subs/summary")
	public ResponseEntity<SubscriptionStatsResponse> getSubscriptionSummary(
			@RequestParam(defaultValue = "7") int days) {

		log.info("📥 GET subscription stats summary");

		return ResponseEntity.ok(dashboardService.getStatsSummary(days));
	}

	@GetMapping("/payment/summary")
	public ResponseEntity<PaymentSummaryDto> getPaymentSummary() {

		log.info("📥 GET payment summary");

		return ResponseEntity.ok(service.getSummary());
	}
}
