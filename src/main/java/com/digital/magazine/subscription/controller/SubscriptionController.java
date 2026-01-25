package com.digital.magazine.subscription.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.digital.magazine.subscription.dto.BuySubscriptionRequest;
import com.digital.magazine.subscription.service.SubscriptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

	private final SubscriptionService subscriptionService;

	@PostMapping("/buy")
	public ResponseEntity<?> buy(@RequestBody BuySubscriptionRequest req, Authentication auth) {

		log.info("Subscription buy request | user={}", auth.getName());

		subscriptionService.buy(req, auth);

		return ResponseEntity.ok("Subscription activated");
	}

}
