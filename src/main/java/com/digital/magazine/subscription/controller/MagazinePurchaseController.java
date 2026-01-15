package com.digital.magazine.subscription.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.subscription.service.MagazinePurchaseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/magazines")
@RequiredArgsConstructor
@Slf4j
public class MagazinePurchaseController {

	private final MagazinePurchaseService service;

	@PostMapping("/{bookId}/buy")
	public ResponseEntity<?> buySingle(@PathVariable Long bookId, Authentication auth) {

		log.info("Single magazine buy | user={} | book={}", auth.getName(), bookId);

		service.purchase(auth, bookId);

		return ResponseEntity.ok("Magazine unlocked");
	}
}
