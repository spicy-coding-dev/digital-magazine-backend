package com.digital.magazine.book.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookUpdateRequestDto {

	private String title;
	private String subTitle;
	private String category;
	private String author;

	private Boolean paid;
	private Double price;

	private Long magazineNo;
	private List<String> tags;

}
