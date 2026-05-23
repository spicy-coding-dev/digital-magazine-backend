package com.digital.magazine.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.admin.dto.DashboardStatsDto;
import com.digital.magazine.admin.dto.SubscriptionStatsResponse;
import com.digital.magazine.admin.service.AdminDashboardService;
import com.digital.magazine.common.response.ApiResponse;
import com.digital.magazine.payment.dto.PaymentAdminResponseDto;
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
	public ResponseEntity<ApiResponse<SubscriptionStatsResponse>> getSubscriptionSummary() {

		log.info("📥 GET subscription stats summary");

		return ResponseEntity.ok(new ApiResponse<>(dashboardService.getStatsSummary()));
	}

	@GetMapping("/payment/summary")
	public ResponseEntity<ApiResponse<PaymentSummaryDto>> getPaymentSummary() {

		log.info("📥 GET payment summary");

		return ResponseEntity.ok(new ApiResponse<>(service.getSummary()));
	}

	@GetMapping("/payments")
	public ResponseEntity<ApiResponse<List<PaymentAdminResponseDto>>> getAllPayments() {

		log.info("📥 Admin payment list request received");

		List<PaymentAdminResponseDto> response = service.getAllPayments();

		log.info("✅ Payment list response sent | size={}", response.size());

		return ResponseEntity.ok(new ApiResponse<>("Payments fetched successfully", response));
	}
}
