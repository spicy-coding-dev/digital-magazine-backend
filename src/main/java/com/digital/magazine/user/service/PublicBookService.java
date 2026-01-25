package com.digital.magazine.user.service;

import java.util.List;
import java.util.Map;

import com.digital.magazine.user.dto.BookSummaryDto;
import com.digital.magazine.user.dto.UserBookFullDto;

public interface PublicBookService {

//	public UserBookFullDto getFullBook(Long bookId);

	public Map<String, List<BookSummaryDto>> getHomeBooks();

}
