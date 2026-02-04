package com.digital.magazine.book.dto;

import java.time.LocalDateTime;

import com.digital.magazine.common.enums.BookStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSummaryDto {

	private Long id;
	private String title;
	private String subTitle;
	private String author;
	private String category;
	private String coverImage;
	private Long magazineNo;

	private boolean paid;
	private Double price;

	private BookStatus status;

	private boolean accessible; // 🔓 open or 🔒 locked

	private LocalDateTime uploadAt;

}
