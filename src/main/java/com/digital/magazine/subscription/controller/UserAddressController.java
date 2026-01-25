package com.digital.magazine.subscription.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.subscription.dto.SaveAddressRequestDto;
import com.digital.magazine.subscription.service.UserAddressService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Slf4j
public class UserAddressController {

	private final UserAddressService addressService;

	@PostMapping("/save")
	public ResponseEntity<?> saveAddress(@RequestBody @Valid SaveAddressRequestDto dto, Authentication auth) {

		log.info("📥 POST /addresses | user={}", auth.getName());

		return ResponseEntity.ok(addressService.saveAddress(dto, auth));
	}

	@GetMapping("/me")
	public ResponseEntity<?> getMyAddresses(Authentication auth) {

		log.info("📥 GET /addresses/me | user={}", auth.getName());

		return ResponseEntity.ok(addressService.getMyAddresses(auth));
	}
}
