package com.digital.magazine.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.digital.magazine.admin.dto.BookUploadRequestDto;
import com.digital.magazine.admin.service.BookService;
import com.digital.magazine.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/books")
@RequiredArgsConstructor
public class BookAdminController {

	private final BookService bookService;

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping(value = "/upload")
	public ResponseEntity<ApiResponse<String>> uploadBook(

			@RequestPart("data") @Valid BookUploadRequestDto dto,

			@RequestPart("coverImage") MultipartFile coverImage,

			@RequestPart("contentPdf") MultipartFile contentPdf,

			@AuthenticationPrincipal UserDetails userDetails) {
		bookService.uploadBook(dto, coverImage, contentPdf, userDetails);
		return ResponseEntity.ok(new ApiResponse<>("📘 புத்தகம் வெற்றிகரமாக பதிவேற்றப்பட்டது"));
	}

}
