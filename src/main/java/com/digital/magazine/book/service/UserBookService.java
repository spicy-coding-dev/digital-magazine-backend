package com.digital.magazine.book.service;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;

import com.digital.magazine.book.dto.BookDetailsWithRelatedResponseDto;
import com.digital.magazine.book.dto.BookSummaryDto;

public interface UserBookService {

	public Map<String, List<BookSummaryDto>> getHomePage(Principal loginUser);

	public List<BookSummaryDto> getBooksByCategory(String categoryLabel, String status, Principal loginUser);

	public BookDetailsWithRelatedResponseDto getBookDetails(Long bookId, Authentication auth);

}
