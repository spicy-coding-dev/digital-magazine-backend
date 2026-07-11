package com.digital.magazine.book.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommentPageResponseDto {

	private List<CommentResponseDto> comments;

	private int page;
	private int size;

	private long totalElements;
	private int totalPages;

	private boolean first;
	private boolean last;
	private boolean hasNext;
}
