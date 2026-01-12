package com.digital.magazine.admin.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import com.digital.magazine.admin.dto.BookUploadRequestDto;
import com.digital.magazine.user.dto.BookSummaryDto;

public interface BookService {
	void uploadBook(BookUploadRequestDto dto, MultipartFile coverImage, MultipartFile contentPdf,
			UserDetails userDetails);

	public List<BookSummaryDto> getBooksByCategory(String categoryLabel, String status);
}
