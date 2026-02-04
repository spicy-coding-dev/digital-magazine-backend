package com.digital.magazine.book.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.book.dto.AdminReplyRequestDto;
import com.digital.magazine.book.dto.CommentRequestDto;
import com.digital.magazine.book.dto.CommentResponseDto;
import com.digital.magazine.book.service.BookCommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
public class BookCommentController {

	private final BookCommentService service;

	// USER COMMENT
	@PostMapping("/book/{bookId}")
	public ResponseEntity<CommentResponseDto> comment(@PathVariable Long bookId,
			@RequestBody @Valid CommentRequestDto dto, Authentication auth) {

		return ResponseEntity.ok(service.addUserComment(bookId, dto, auth));
	}

//	// USER / GUEST VIEW
//	@GetMapping("/book/{bookId}")
//	public ResponseEntity<Page<CommentResponseDto>> getComments(@PathVariable Long bookId,
//			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
//
//		return ResponseEntity.ok(service.getBookComments(bookId, pageable));
//	}

//	// ADMIN PENDING LIST
//	@GetMapping("/admin/pending")
//	public ResponseEntity<Page<CommentResponseDto>> pending(@PageableDefault(size = 10) Pageable pageable) {
//
//		return ResponseEntity.ok(service.getPendingCommentsForAdmin(pageable));
//	}

//	// ADMIN REPLY
//	@PostMapping("/admin/reply/{commentId}")
//	public ResponseEntity<CommentResponseDto> reply(@PathVariable Long commentId,
//			@RequestBody @Valid AdminReplyRequestDto dto) {
//
//		return ResponseEntity.ok(service.replyByAdmin(commentId, dto));
//	}
//
//	// USER DELETE
//	@DeleteMapping("/{commentId}")
//	public ResponseEntity<Void> delete(@PathVariable Long commentId, Authentication auth) {
//
//		service.deleteComment(commentId, auth);
//		return ResponseEntity.noContent().build();
//	}
}
