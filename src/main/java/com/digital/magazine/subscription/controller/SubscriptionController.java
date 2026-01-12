package com.digital.magazine.subscription.controller;

import java.util.Map;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.digital.magazine.subscription.dto.SubscriptionPlanDto;
import com.digital.magazine.subscription.service.SubscriptionPlanService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

	private final SubscriptionPlanService service;

	@GetMapping("/getplans")
	public Map<String, List<SubscriptionPlanDto>> getPlans() {

		log.info("📥 GET /api/subscriptions/getplans called");

		Map<String, List<SubscriptionPlanDto>> response = service.getActivePlans();

		log.info("📤 Subscription plans fetched successfully. Categories={}", response.keySet());

		return response;
	}
}
