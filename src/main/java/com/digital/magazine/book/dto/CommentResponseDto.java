package com.digital.magazine.book.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommentResponseDto {

	private Long commentId;
	private Long bookId;

	private String username;
	private String userComment;

	private boolean adminReplied;
	private String adminReply;

	private LocalDateTime createdAt;
	private LocalDateTime adminRepliedAt;
}
