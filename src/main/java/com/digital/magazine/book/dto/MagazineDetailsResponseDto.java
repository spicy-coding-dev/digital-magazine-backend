package com.digital.magazine.book.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MagazineDetailsResponseDto {

	private Long magazineNo;

	private String title;

	private String coverImage;

	private List<BookDetailsResponseDto> articles;

	private List<String> tags;

}
