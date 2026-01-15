package com.digital.magazine.user.dto;

import java.util.List;

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

	// 🔥 IMPORTANT: Entity illa, plain String list
	private List<String> tags;

}
