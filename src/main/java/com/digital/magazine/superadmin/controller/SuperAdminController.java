package com.digital.magazine.superadmin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.common.response.ApiResponse;
import com.digital.magazine.superadmin.dto.CreateAdminRequestDto;
import com.digital.magazine.superadmin.service.SuperAdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {

	private final SuperAdminService superAdminService;

	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@PostMapping("/create-admin")
	public ResponseEntity<ApiResponse<String>> createAdmin(@Valid @RequestBody CreateAdminRequestDto dto) {

		log.info("📥 Request received to create admin");

		superAdminService.createAdmin(dto);

		return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
				"Admin பதிவு வெற்றிகரமாக நிறைவடைந்தது. தயவுசெய்து உங்கள் மின்னஞ்சலை உறுதிப்படுத்தவும்."));
	}

	@GetMapping("/verify-email")
	public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {

		String result = superAdminService.verifyEmail(token);

		return ResponseEntity.ok(new ApiResponse<>("Email verification status", result));
	}

}
