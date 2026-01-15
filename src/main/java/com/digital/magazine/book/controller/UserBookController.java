package com.digital.magazine.book.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.book.service.UserBookService;
import com.digital.magazine.common.response.ApiResponse;
import com.digital.magazine.user.dto.BookSummaryDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/")
public class UserBookController {

	private final UserBookService bookService;

	@GetMapping("/home")
	public ResponseEntity<ApiResponse<Map<String, List<BookSummaryDto>>>> home() {
		return ResponseEntity.ok(new ApiResponse<>(bookService.getHomePage()));
	}

}
