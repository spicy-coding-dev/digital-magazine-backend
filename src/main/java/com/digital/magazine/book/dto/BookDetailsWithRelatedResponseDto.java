package com.digital.magazine.book.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookDetailsWithRelatedResponseDto {

	private BookDetailsResponseDto book;

	private List<BookSummaryDto> relatedBooks;
}
