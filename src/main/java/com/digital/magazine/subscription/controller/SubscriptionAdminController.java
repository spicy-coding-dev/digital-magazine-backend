package com.digital.magazine.subscription.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.digital.magazine.subscription.dto.SubscriptionPlanDto;
import com.digital.magazine.subscription.dto.SubscriptionUpdateRequest;
import com.digital.magazine.subscription.service.SubscriptionPlanService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionAdminController {

	private final SubscriptionPlanService service;

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PatchMapping("/update/{planCode}")
	public ResponseEntity<SubscriptionPlanDto> updatePlan(@PathVariable String planCode,
			@RequestBody SubscriptionUpdateRequest request) {

		log.info("✏️ Updating subscription plan: {}", planCode);

		return ResponseEntity.ok(service.updatePlan(planCode, request));
	}
}
