package com.digital.magazine.book.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.digital.magazine.book.dto.BookDetailsWithRelatedResponseDto;
import com.digital.magazine.book.dto.BookStatusUpdateDto;
import com.digital.magazine.book.dto.BookSummaryDto;
import com.digital.magazine.book.dto.BookUpdateRequestDto;
import com.digital.magazine.book.dto.BookUploadRequestDto;
import com.digital.magazine.book.service.BookService;
import com.digital.magazine.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/books")
@RequiredArgsConstructor
public class AdminBookController {

	private final BookService bookService;

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping(value = "/upload")
	public ResponseEntity<ApiResponse<String>> uploadBook(

			@RequestPart("data") @Valid BookUploadRequestDto dto,

			@RequestPart("coverImage") MultipartFile coverImage,

			@AuthenticationPrincipal UserDetails userDetails) {
		bookService.uploadBook(dto, coverImage, userDetails);
		return ResponseEntity.ok(new ApiResponse<>("📘 புத்தகம் வெற்றிகரமாக பதிவேற்றப்பட்டது"));
	}

	@PostMapping("/editor-image")
	public ResponseEntity<ApiResponse<Map<String, String>>> uploadEditorImage(@RequestParam MultipartFile image) {

		log.info("🖼️ [CONTROLLER] Editor image upload request");

		String imageUrl = bookService.uploadEditorImage(image);

		log.info("🚀 [CONTROLLER] Editor image upload success");

		return ResponseEntity.ok(new ApiResponse<>(Map.of("url", imageUrl)));
	}

	// 🔥 AUTO SAVE + MANUAL SAVE
	@PostMapping("/{bookId}/content")
	public ResponseEntity<ApiResponse<String>> saveContent(@PathVariable Long bookId,
			@RequestBody Map<String, String> body) {

		log.info("📥 [CONTROLLER] Save content | bookId={}", bookId);

		bookService.saveOrUpdateContent(bookId, body.get("content"));

		return ResponseEntity.ok(new ApiResponse<>("📘 புத்தகத்தின் உள்ளடக்கம் வெற்றிகரமாக பதிவேற்றப்பட்டது"));
	}

	// 🔥 LOAD CONTENT
	@GetMapping("/{bookId}/content")
	public ResponseEntity<ApiResponse<Map<String, String>>> getContent(@PathVariable Long bookId) {

		log.info("📤 [CONTROLLER] Get content | bookId={}", bookId);

		String content = bookService.getContentByBookId(bookId);

		return ResponseEntity.ok(new ApiResponse<>("புத்தக உள்ளடக்கம் ஏற்றப்பட்டது", Map.of("content", content)));
	}

	@GetMapping("/{bookId}")
	public ResponseEntity<ApiResponse<BookDetailsWithRelatedResponseDto>> getBookDetails(@PathVariable Long bookId) {

		log.info("📘 [CONTROLLER] Get book details | bookId={}", bookId);

		BookDetailsWithRelatedResponseDto response = bookService.getBookDetails(bookId);

		return ResponseEntity.ok(new ApiResponse<>(response));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/category")
	public ResponseEntity<ApiResponse<List<BookSummaryDto>>> getBooksByCategory(@RequestParam String category,
			@RequestParam String status) {

		log.info("🌐 API hit | GET /api/books/category | category={}", category);

		List<BookSummaryDto> books = bookService.getBooksByCategory(category, status);

		return ResponseEntity.ok(new ApiResponse<>("Books fetched successfully", books));
	}

	@PatchMapping("/{bookId}")
	public ResponseEntity<ApiResponse<BookSummaryDto>> updateBook(@PathVariable Long bookId,
			@RequestBody @Valid BookUpdateRequestDto dto, Authentication auth) {

		log.info("ADMIN [{}] editing bookId={}", auth.getName(), bookId);

		BookSummaryDto response = bookService.updateBook(bookId, dto, auth);

		return ResponseEntity.ok(new ApiResponse<>("✅ புத்தகம் வெற்றிகரமாக புதுப்பிக்கப்பட்டது", response));
	}

	@PutMapping("/{bookId}/cover-image")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<String>> updateCoverImage(@PathVariable Long bookId,
			@RequestParam MultipartFile file, Authentication auth) {

		log.info("ADMIN [{}] updating cover image for bookId={}", auth.getName(), bookId);

		String imageUrl = bookService.updateCoverImage(bookId, file, auth);

		return ResponseEntity.ok(new ApiResponse<>("✅ புத்தகத்தின் கவர் படம் வெற்றிகரமாக மாற்றப்பட்டது", imageUrl));
	}

	@PatchMapping("/{bookId}/status")
	public ResponseEntity<ApiResponse<String>> changeStatus(@PathVariable Long bookId,
			@RequestBody @Valid BookStatusUpdateDto dto, Authentication auth) {

		log.info("ADMIN [{}] requested status change | bookId={} | newStatus={}", auth.getName(), bookId,
				dto.getStatus());

		bookService.changeStatus(bookId, dto, auth);

		return ResponseEntity
				.ok(new ApiResponse<>("✅ புத்தகத்தின் நிலை வெற்றிகரமாக மாற்றப்பட்டது", dto.getStatus().name()));
	}

	@DeleteMapping("/{bookId}")
	public ResponseEntity<ApiResponse<String>> deleteBook(@PathVariable Long bookId, Authentication auth) {

		log.info("ADMIN [{}] requested DELETE | bookId={}", auth.getName(), bookId);

		bookService.deleteBook(bookId, auth);

		return ResponseEntity.ok(new ApiResponse<>("✅ புத்தகம் வெற்றிகரமாக நீக்கப்பட்டது", "DELETED"));
	}

}
