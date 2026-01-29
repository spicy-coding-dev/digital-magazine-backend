package com.digital.magazine.book.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import com.digital.magazine.book.dto.BookStatusUpdateDto;
import com.digital.magazine.book.dto.BookSummaryDto;
import com.digital.magazine.book.dto.BookUpdateRequestDto;
import com.digital.magazine.book.dto.BookUploadRequestDto;

public interface BookService {
	void uploadBook(BookUploadRequestDto dto, MultipartFile coverImage, MultipartFile contentPdf,
			UserDetails userDetails);

	public List<BookSummaryDto> getBooksByCategory(String categoryLabel, String status);

	public BookSummaryDto updateBook(Long bookId, BookUpdateRequestDto dto, Authentication auth);

	public String updateCoverImage(Long bookId, MultipartFile file, Authentication auth);

	public void changeStatus(Long bookId, BookStatusUpdateDto dto, Authentication auth);

	public void deleteBook(Long bookId, Authentication auth);
}
