package com.digital.magazine.subscription.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.common.response.ApiResponse;
import com.digital.magazine.subscription.dto.SaveAddressRequestDto;
import com.digital.magazine.subscription.dto.UserAddressResponseDto;
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
	public ResponseEntity<ApiResponse<UserAddressResponseDto>> saveAddress(
			@RequestBody @Valid SaveAddressRequestDto dto, Authentication auth) {

		log.info("📥 POST /addresses | user={}", auth.getName());

		return ResponseEntity.ok(new ApiResponse<>("உங்களுடைய முகவரி வெற்றிகரமாக சேர்க்கப்பட்டது",
				addressService.saveAddress(dto, auth)));
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<List<UserAddressResponseDto>>> getMyAddresses(Authentication auth) {

		log.info("📥 GET /addresses/me | user={}", auth.getName());

		return ResponseEntity.ok(new ApiResponse<>(addressService.getMyAddresses(auth)));
	}
}
