package com.digital.magazine.book.service;

import java.util.List;
import java.util.Map;

import com.digital.magazine.user.dto.BookSummaryDto;

public interface UserBookService {

	public Map<String, List<BookSummaryDto>> getHomePage();

}
