package com.digital.magazine.common.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.digital.magazine.common.enums.MailTargetType;
import com.digital.magazine.common.response.ApiResponse;
import com.digital.magazine.common.service.SendEmailService;
import com.digital.magazine.user.enums.AccountStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class SendEmailController {

	private final SendEmailService service;

	@PostMapping(value = "/send-email", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<String>> sendEmail(

			@RequestParam MailTargetType targetType,

			// ACCOUNT_STATUS ku mattum
			@RequestParam(required = false) AccountStatus accountStatus,

			@RequestParam String subject, @RequestParam String content,

			@RequestParam(required = false) MultipartFile file) throws IOException {

		byte[] attachmentBytes = null;
		String fileName = null;

		if (file != null && !file.isEmpty()) {
			attachmentBytes = file.getBytes();
			fileName = file.getOriginalFilename();
		}

		log.info("📧 Bulk email request | targetType={} | subject={}", targetType, subject);

		service.sendBulkMail(targetType, accountStatus, subject, content, attachmentBytes, fileName);

		return ResponseEntity.ok(new ApiResponse<>("Email sent successfully to " + targetType + " users"));
	}

}
