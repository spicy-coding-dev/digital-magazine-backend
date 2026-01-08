package com.digital.magazine.user.dto;

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
	private String author;
	private String category;
	private String coverImage;

	private boolean paid;
	private Double price;

}
