package com.digital.magazine.book.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.digital.magazine.book.dto.AdminReplyRequestDto;
import com.digital.magazine.book.dto.CommentRequestDto;
import com.digital.magazine.book.dto.CommentResponseDto;
import com.digital.magazine.book.entity.BookComment;
import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.repository.BookCommentRepository;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.book.service.BookCommentService;
import com.digital.magazine.common.exception.UnauthorizedAccessException;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookCommentServiceImpl implements BookCommentService {

	private final BookCommentRepository commentRepo;
	private final BookRepository booksRepo;
	private final UserRepository userRepo;

	// USER COMMENT
	@Override
	public CommentResponseDto addUserComment(Long bookId, CommentRequestDto dto, Authentication auth) {

		log.info("📝 User comment | bookId={} | user={}", bookId, auth.getName());

		Books book = booksRepo.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));

		User user = userRepo.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));

		BookComment comment = BookComment.builder().book(book).user(user).content(dto.getContent()).build();

		commentRepo.save(comment);

		log.info("✅ Comment saved | id={}", comment.getId());
		return map(comment);
	}

//	// USER + GUEST VIEW
//	@Override
//	public Page<CommentResponseDto> getBookComments(Long bookId, Pageable pageable) {
//
//		log.info("📥 Fetch comments | bookId={}", bookId);
//
//		return commentRepo.findByBookIdAndDeletedFalse(bookId, pageable).map(this::map);
//	}
//
//	// ADMIN DASHBOARD (PENDING ONLY)
//	@Override
//	public Page<CommentResponseDto> getPendingCommentsForAdmin(Pageable pageable) {
//
//		log.info("🛠 Fetch pending admin comments");
//
//		return commentRepo.findByAdminRepliedFalseAndDeletedFalse(pageable).map(this::map);
//	}

//	// ADMIN REPLY
//	@Override
//	public CommentResponseDto replyByAdmin(Long commentId, AdminReplyRequestDto dto) {
//
//		log.info("💬 Admin replying | commentId={}", commentId);
//
//		BookComment comment = commentRepo.findById(commentId)
//				.orElseThrow(() -> new RuntimeException("Comment not found"));
//
//		comment.setAdminReply(dto.getReply());
//		comment.setAdminReplied(true);
//		comment.setAdminRepliedAt(LocalDateTime.now());
//
//		commentRepo.save(comment);
//
//		log.info("✅ Admin replied | commentId={}", commentId);
//		return map(comment);
//	}

	// USER DELETE (SOFT)
//	@Override
//	public void deleteComment(Long commentId, Authentication auth) {
//
//		log.info("🗑 Delete comment | id={} | user={}", commentId, auth.getName());
//
//		BookComment comment = commentRepo.findByIdAndDeletedFalse(commentId)
//				.orElseThrow(() -> new RuntimeException("Comment not found"));
//
//		if (!comment.getUser().getName().equals(auth.getName())) {
//			log.warn("⛔ Unauthorized delete attempt");
//			throw new UnauthorizedAccessException("Not allowed");
//		}
//
//		comment.setDeleted(true);
//		commentRepo.save(comment);
//
//		log.info("✅ Comment soft deleted | id={}", commentId);
//	}

	private CommentResponseDto map(BookComment c) {
		return CommentResponseDto.builder().commentId(c.getId()).bookId(c.getBook().getId())
				.username(c.getUser().getName()).userComment(c.getContent()).adminReplied(c.isAdminReplied())
				.adminReply(c.getAdminReply()).createdAt(c.getCreatedAt()).adminRepliedAt(c.getAdminRepliedAt())
				.build();
	}
}
