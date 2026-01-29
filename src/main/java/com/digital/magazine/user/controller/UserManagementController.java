package com.digital.magazine.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.common.enums.Role;
import com.digital.magazine.common.response.ApiResponse;
import com.digital.magazine.user.dto.CreateUserDto;
import com.digital.magazine.user.dto.UserSetPasswordDto;
import com.digital.magazine.user.service.UserCreationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/manage")
@RequiredArgsConstructor
public class UserManagementController {

	private final UserCreationService userCreationService;

	// 🔹 SUPER ADMIN → CREATE ADMIN
	@PostMapping("/create-admin")
	public ResponseEntity<ApiResponse<String>> createAdmin(@RequestBody @Valid CreateUserDto dto, Authentication auth) {

		log.info("SUPER_ADMIN [{}] requested ADMIN creation for email={}", auth.getName(), dto.getEmail());

		userCreationService.createUser(dto, Role.ADMIN, auth);

		log.info("ADMIN creation initiated successfully for email={}", dto.getEmail());

		return ResponseEntity.ok(new ApiResponse<>("நிர்வாகி உருவாக்கம்",
				"✅ நிர்வாகி கணக்கு வெற்றிகரமாக உருவாக்கப்பட்டது. உறுதிப்படுத்தல் மின்னஞ்சல் அனுப்பப்பட்டுள்ளது."));
	}

	// 🔹 ADMIN → CREATE USER
	@PostMapping("/create-user")
	public ResponseEntity<ApiResponse<String>> createUser(@RequestBody @Valid CreateUserDto dto, Authentication auth) {

		log.info("ADMIN [{}] requested USER creation for email={}", auth.getName(), dto.getEmail());

		userCreationService.createUser(dto, Role.USER, auth);

		log.info("USER creation initiated successfully for email={}", dto.getEmail());

		return ResponseEntity.ok(new ApiResponse<>("பயனர் உருவாக்கம்",
				"✅ பயனர் கணக்கு வெற்றிகரமாக உருவாக்கப்பட்டது. உறுதிப்படுத்தல் மின்னஞ்சல் அனுப்பப்பட்டுள்ளது."));
	}

	@PostMapping("/verify-email")
	public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestBody UserSetPasswordDto setPassword) {

		String result = userCreationService.verifyEmailAndSetPassword(setPassword);

		return ResponseEntity.ok(new ApiResponse<>("Email verification status", result));
	}

}
