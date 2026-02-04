package com.digital.magazine.book.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BookDetailsResponseDto {

	private Long id;

	private String title;

	private String subtitle;

	private String authorName;

	private Long magazineNo;

	private String content; // HTML

	private LocalDateTime publishedAt;

	private String status;

	private List<String> tags;
}
