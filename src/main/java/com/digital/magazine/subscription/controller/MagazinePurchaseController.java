package com.digital.magazine.subscription.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.common.response.ApiResponse;
import com.digital.magazine.subscription.service.MagazinePurchaseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
@Slf4j
public class MagazinePurchaseController {

	private final MagazinePurchaseService service;

	@PostMapping("magazine/{bookId}/buy")
	public ResponseEntity<ApiResponse<String>> buySingle(@PathVariable Long bookId, Authentication auth) {

		log.info("Single magazine buy | user={} | book={}", auth.getName(), bookId);

		String message = service.purchase(auth, bookId);

		return ResponseEntity.ok(new ApiResponse<>(message));
	}
}
