package com.digital.magazine.subscription.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.digital.magazine.common.response.ApiResponse;
import com.digital.magazine.subscription.dto.MagazinePurchaseAdminDto;
import com.digital.magazine.subscription.dto.SubscribedUserDto;
import com.digital.magazine.subscription.dto.SubscriptionPlanDto;
import com.digital.magazine.subscription.dto.SubscriptionUpdateRequest;
import com.digital.magazine.subscription.enums.SubscriptionStatus;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.subscription.service.SubscriptionPlanService;
import com.digital.magazine.subscription.service.SubscriptionQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionAdminController {

	private final SubscriptionPlanService service;
	private final SubscriptionQueryService queryService;

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PatchMapping("/update/{planCode}")
	public ResponseEntity<ApiResponse<SubscriptionPlanDto>> updatePlan(@PathVariable String planCode,
			@RequestBody SubscriptionUpdateRequest request) {

		log.info("✏️ Updating subscription plan: {}", planCode);

		SubscriptionPlanDto updatedPlan = service.updatePlan(planCode, request);

		return ResponseEntity.ok(new ApiResponse<>("சந்தா திட்டம் வெற்றிகரமாக புதுப்பிக்கப்பட்டது", updatedPlan));
	}

	@GetMapping("/getplans")
	public ResponseEntity<ApiResponse<Map<String, List<SubscriptionPlanDto>>>> getPlans() {

		log.info("📥 GET /api/subscriptions/getplans called");

		Map<String, List<SubscriptionPlanDto>> response = service.getActivePlans();

		log.info("📤 Subscription plans fetched successfully. Categories={}", response.keySet());

		return ResponseEntity.ok(new ApiResponse<>("செயலில் உள்ள சந்தா திட்டங்கள் பெறப்பட்டன", response));
	}

	@GetMapping("/users")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<ApiResponse<List<SubscribedUserDto>>> getSubscribedUsers(
			@RequestParam SubscriptionStatus status, @RequestParam(required = false) SubscriptionType type) {

		log.info("👮 Admin requested subscribed users list");

		return ResponseEntity.ok(new ApiResponse<>(queryService.getSubscribedUsers(type, status)));
	}

	// 🔹 All single magazine purchases
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/magazine/users")
	public ResponseEntity<ApiResponse<List<MagazinePurchaseAdminDto>>> getAllPurchases() {

		log.info("📥 GET /admin/purchases");

		return ResponseEntity.ok(new ApiResponse<>(queryService.getAllPurchases()));
	}

	// 🔹 Purchases by magazine
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/book/{bookId}")
	public ResponseEntity<?> getByBook(@PathVariable Long bookId) {

		log.info("📥 GET /admin/purchases/book/{}", bookId);

		return ResponseEntity.ok(queryService.getPurchasesByBook(bookId));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/between-dates")
	public ResponseEntity<ApiResponse<List<MagazinePurchaseAdminDto>>> getPurchasesBetweenDates(

			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

			@RequestParam(required = false) Long bookId) {

		log.info("📥 GET purchases between dates | from={} | to={} | bookId={}", fromDate, toDate, bookId);

		return ResponseEntity.ok(new ApiResponse<>(queryService.getPurchasesBetweenDates(fromDate, toDate, bookId)));
	}
}
