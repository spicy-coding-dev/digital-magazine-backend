package com.digital.magazine.book.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.book.dto.BookSummaryDto;
import com.digital.magazine.book.service.UserBookService;
import com.digital.magazine.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/")
public class UserBookController {

	private final UserBookService bookService;

	@GetMapping("/home")
	public ResponseEntity<ApiResponse<Map<String, List<BookSummaryDto>>>> home(Principal user) {
		return ResponseEntity.ok(new ApiResponse<>(bookService.getHomePage(user)));
	}

	@GetMapping("/books/category")
	public ResponseEntity<ApiResponse<List<BookSummaryDto>>> getBooksByCategory(@RequestParam String category,
			@RequestParam String status, Principal user) {
		List<BookSummaryDto> books = bookService.getBooksByCategory(category, status, user);

		return ResponseEntity.ok(new ApiResponse<>("Books fetched successfully", books));
	}

	@GetMapping("/books/{bookId}/content")
	public ResponseEntity<ApiResponse<String>> getBookContent(@PathVariable Long bookId, Authentication auth) {

		log.info("📥 Get book content request | bookId={} | user={}", bookId, auth != null ? auth.getName() : "GUEST");

		String signedUrl = bookService.getBookContentPdf(bookId, auth);

		return ResponseEntity.ok(new ApiResponse<>("PDF access granted", signedUrl));
	}

}
