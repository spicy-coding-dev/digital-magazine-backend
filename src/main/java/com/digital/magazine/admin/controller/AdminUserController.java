package com.digital.magazine.admin.controller;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.digital.magazine.admin.dto.AdminUserDto;
import com.digital.magazine.admin.dto.UserBlockRequestDto;
import com.digital.magazine.admin.service.AdminUserService;
import com.digital.magazine.common.response.ApiResponse;
import com.digital.magazine.user.enums.AccountStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminUserController {

	private final AdminUserService service;

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/all-users")
	public ResponseEntity<Page<AdminUserDto>> getAllUsers(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		log.info("📥 API called: GET /admin/users");

		Page<AdminUserDto> users = service.getAllUsers(PageRequest.of(page, size));

		return ResponseEntity.ok(users);
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PatchMapping("users/{userId}/block-toggle")
	public ResponseEntity<ApiResponse<String>> toggleUserBlock(@PathVariable Long userId,
			@RequestBody UserBlockRequestDto request) {

		log.info("🛡️ Admin triggered block-toggle | userId={}", userId);

		service.toggleUserBlock(userId, request.getReason());

		return ResponseEntity.ok(new ApiResponse<>("User block status updated successfully"));
	}

//	@PostMapping(value = "/send-email", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public ResponseEntity<ApiResponse<String>> sendEmailToUsers(@RequestParam AccountStatus status,
//			@RequestParam String subject, @RequestParam String content,
//			@RequestParam(required = false) MultipartFile file) throws IOException {
//
//		byte[] attachmentBytes = null;
//		String fileName = null;
//
//		if (file != null && !file.isEmpty()) {
//			attachmentBytes = file.getBytes(); // 🔥 IMPORTANT
//			fileName = file.getOriginalFilename();
//		}
//
//		log.info("📧 Admin triggered bulk email | status={}, subject={}, filePresent={}", status, subject,
//				file != null && !file.isEmpty());
//
//		service.sendBulkMailByStatus(status, subject, content, attachmentBytes, fileName);
//
//		return ResponseEntity.ok(new ApiResponse<>("Email sent successfully to " + status + " users"));
//	}

}
