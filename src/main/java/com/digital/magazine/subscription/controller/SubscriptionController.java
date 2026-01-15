package com.digital.magazine.subscription.controller;

import java.util.Map;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.digital.magazine.subscription.dto.BuySubscriptionRequest;
import com.digital.magazine.subscription.dto.SubscriptionPlanDto;
import com.digital.magazine.subscription.service.AccessService;
import com.digital.magazine.subscription.service.SubscriptionPlanService;
import com.digital.magazine.subscription.service.SubscriptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

	private final SubscriptionService subscriptionService;
	private final SubscriptionPlanService subscriptionPlanService;

	@GetMapping("/getplans")
	public Map<String, List<SubscriptionPlanDto>> getPlans() {

		log.info("📥 GET /api/subscriptions/getplans called");

		Map<String, List<SubscriptionPlanDto>> response = subscriptionPlanService.getActivePlans();

		log.info("📤 Subscription plans fetched successfully. Categories={}", response.keySet());

		return response;
	}

	@PostMapping("/buy")
	public ResponseEntity<?> buy(@RequestBody BuySubscriptionRequest req, Authentication auth) {

		log.info("Subscription buy request | user={}", auth.getName());

		subscriptionService.buy(req, auth);

		return ResponseEntity.ok("Subscription activated");
	}

}
