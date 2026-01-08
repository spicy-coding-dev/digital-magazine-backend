package com.digital.magazine.user.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.common.response.ApiResponse;
import com.digital.magazine.user.dto.BookSummaryDto;
import com.digital.magazine.user.dto.UserBookFullDto;
import com.digital.magazine.user.service.UserBookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/")
public class UserBookController {

	private final UserBookService bookService;

	@GetMapping("/books/{bookId}/full")
	public ResponseEntity<ApiResponse<UserBookFullDto>> getFullBook(@PathVariable Long bookId) {

		return ResponseEntity.ok(new ApiResponse<>(bookService.getFullBook(bookId)));
	}
	
	@GetMapping("/home/books")
	public ResponseEntity<ApiResponse<Map<String, List<BookSummaryDto>>>> getHomeBooks() {

		return ResponseEntity.ok(
			new ApiResponse<>(bookService.getHomeBooks())
		);
	}


}
