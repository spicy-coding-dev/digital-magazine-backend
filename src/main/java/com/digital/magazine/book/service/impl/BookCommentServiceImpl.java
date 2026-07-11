package com.digital.magazine.book.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.digital.magazine.book.dto.AdminReplyRequestDto;
import com.digital.magazine.book.dto.CommentPageResponseDto;
import com.digital.magazine.book.dto.CommentRequestDto;
import com.digital.magazine.book.dto.CommentResponseDto;
import com.digital.magazine.book.entity.BookComment;
import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.repository.BookCommentRepository;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.book.service.BookCommentService;
import com.digital.magazine.common.enums.BookStatus;
import com.digital.magazine.common.enums.Role;
import com.digital.magazine.common.exception.BookNotPublishedException;
import com.digital.magazine.common.exception.CommentAlreadyRepliedException;
import com.digital.magazine.common.exception.CommentNotFoundException;
import com.digital.magazine.common.exception.InvalidCommentException;
import com.digital.magazine.common.exception.InvalidCommentReplyException;
import com.digital.magazine.common.exception.NoBooksFoundException;
import com.digital.magazine.common.exception.UnauthorizedAccessException;
import com.digital.magazine.common.exception.UserNotFoundException;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookCommentServiceImpl implements BookCommentService {

	private final BookCommentRepository commentRepo;
	private final BookRepository booksRepo;
	private final UserRepository userRepo;

	// USER COMMENT
	@Override
	public CommentResponseDto addUserComment(Long bookId, CommentRequestDto dto, Authentication auth) {

		log.info("📝 User Comment | bookId={} | user={}", bookId, auth.getName());

		Books book = booksRepo.findById(bookId).orElseThrow(() -> new NoBooksFoundException("புத்தகம் கிடைக்கவில்லை"));

		if (book.getStatus() != BookStatus.PUBLISHED) {

			log.warn("⚠️ Book Not Published | bookId={}", bookId);

			throw new BookNotPublishedException("இந்த புத்தகம் இன்னும் வெளியிடப்படவில்லை");
		}

		if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {

			log.warn("⚠️ Empty Comment | bookId={} | user={}", bookId, auth.getName());

			throw new InvalidCommentException("கருத்து காலியாக இருக்கக்கூடாது.");
		}

		User user = userRepo.findByEmail(auth.getName())
				.orElseThrow(() -> new UserNotFoundException("பயனர் கிடைக்கவில்லை"));

		if (user.getRole() == Role.ADMIN) {

			log.warn("🚫 Admin Comment Attempt | admin={}", user.getEmail());

			throw new UnauthorizedAccessException("நிர்வாகிகள் கருத்து பதிவிட அனுமதிக்கப்படமாட்டார்கள்.");
		}

		BookComment comment = BookComment.builder().book(book).user(user).content(dto.getContent().trim()).build();

		commentRepo.save(comment);

		log.info("✅ Comment Saved Successfully | commentId={} | userId={}", comment.getId(), user.getId());

		return map(comment);
	}

	// USER + GUEST VIEW
	@Override
	@Transactional(readOnly = true)
	public CommentPageResponseDto getBookComments(Long bookId, Pageable pageable) {

		log.info("📥 Fetch comments | bookId={}", bookId);

		Page<CommentResponseDto> page = commentRepo.findByBookIdAndDeletedFalse(bookId, pageable).map(this::map);

		return mapToCommentPage(page);
	}

	// ADMIN DASHBOARD (PENDING ONLY)
	@Override
	@Transactional(readOnly = true)
	public CommentPageResponseDto getPendingCommentsForAdmin(Pageable pageable) {

		log.info("🛠 Fetch pending comments");

		Page<CommentResponseDto> page = commentRepo.findByAdminRepliedFalseAndDeletedFalse(pageable).map(this::map);

		return mapToCommentPage(page);
	}

//	// ADMIN REPLY
	@Override
	@Transactional
	public CommentResponseDto replyByAdmin(Long commentId, AdminReplyRequestDto dto) {

		log.info("💬 Admin Reply Request | commentId={}", commentId);

		BookComment comment = commentRepo.findByIdAndDeletedFalse(commentId).orElseThrow(() -> {
			log.warn("❌ Comment Not Found | commentId={}", commentId);
			return new CommentNotFoundException("கருத்து கிடைக்கவில்லை.");
		});

		if (comment.isAdminReplied()) {

			log.warn("⚠️ Already Replied | commentId={}", commentId);

			throw new CommentAlreadyRepliedException("இந்த கருத்திற்கு ஏற்கனவே பதிலளிக்கப்பட்டுள்ளது.");
		}

		if (dto.getReply() == null || dto.getReply().trim().isEmpty()) {

			log.warn("⚠️ Empty Admin Reply | commentId={}", commentId);

			throw new InvalidCommentReplyException("பதில் காலியாக இருக்கக்கூடாது.");
		}

		comment.setAdminReply(dto.getReply().trim());
		comment.setAdminReplied(true);
		comment.setAdminRepliedAt(LocalDateTime.now());

		commentRepo.save(comment);

		log.info("✅ Admin Reply Saved | commentId={}", commentId);

		return map(comment);
	}

	// USER DELETE (SOFT)
	@Override
	public void deleteComment(Long commentId, Authentication auth) {

		log.info("🗑 Delete comment | id={} | user={}", commentId, auth.getName());

		BookComment comment = commentRepo.findByIdAndDeletedFalse(commentId)
				.orElseThrow(() -> new CommentNotFoundException("கருத்து கிடைக்கவில்லை."));

		if (!comment.getUser().getEmail().equals(auth.getName())) {

			log.warn("⛔ Unauthorized delete | commentId={} | user={}", commentId, auth.getName());

			throw new UnauthorizedAccessException("இந்த கருத்தை நீக்க உங்களுக்கு அனுமதி இல்லை.");
		}

		comment.setDeleted(true);
		commentRepo.save(comment);

		log.info("✅ Comment soft deleted | id={}", commentId);
	}

	private CommentResponseDto map(BookComment comment) {

		return CommentResponseDto.builder().commentId(comment.getId()).bookId(comment.getBook().getId())
				.username(comment.getUser().getName()).userComment(comment.getContent())
				.adminReplied(comment.isAdminReplied()).adminReply(comment.getAdminReply())
				.createdAt(comment.getCreatedAt()).adminRepliedAt(comment.getAdminRepliedAt()).build();
	}

	private CommentPageResponseDto mapToCommentPage(Page<CommentResponseDto> page) {

		return CommentPageResponseDto.builder().comments(page.getContent()).page(page.getNumber()).size(page.getSize())
				.totalElements(page.getTotalElements()).totalPages(page.getTotalPages()).first(page.isFirst())
				.last(page.isLast()).hasNext(page.hasNext()).build();
	}
}
