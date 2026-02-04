package com.digital.magazine.book.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import com.digital.magazine.book.dto.AdminReplyRequestDto;
import com.digital.magazine.book.dto.CommentRequestDto;
import com.digital.magazine.book.dto.CommentResponseDto;

public interface BookCommentService {

	CommentResponseDto addUserComment(Long bookId, CommentRequestDto dto, Authentication auth);
//
//	Page<CommentResponseDto> getBookComments(Long bookId, Pageable pageable);
//
//	Page<CommentResponseDto> getPendingCommentsForAdmin(Pageable pageable);

//	CommentResponseDto replyByAdmin(Long commentId, AdminReplyRequestDto dto);
//
//	void deleteComment(Long commentId, Authentication auth);

}
