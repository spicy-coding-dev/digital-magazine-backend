package com.digital.magazine.user.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.digital.magazine.admin.entity.Books;
import com.digital.magazine.admin.repository.BookContentBlockRepository;
import com.digital.magazine.admin.repository.BookRepository;

import com.digital.magazine.common.enums.BookStatus;
import com.digital.magazine.user.dto.BookContentDto;
import com.digital.magazine.user.dto.BookSummaryDto;
import com.digital.magazine.user.dto.UserBookFullDto;
import com.digital.magazine.user.service.UserBookService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional // 🔥 ADD THIS
public class UserBookServiceImpl implements UserBookService {

	private final BookRepository bookRepo;
	private final BookContentBlockRepository blockRepo;

	@Override
	public UserBookFullDto getFullBook(Long bookId) {

		Books book = bookRepo.findById(bookId).filter(b -> b.getStatus() == BookStatus.PUBLISHED)
				.orElseThrow(() -> new RuntimeException("Book not found"));

		List<BookContentDto> contents = blockRepo.findByBookIdOrderByBlockOrder(bookId).stream()
				.map(b -> BookContentDto.builder().order(b.getBlockOrder()).type(b.getType()).text(b.getTextContent())
						.imageUrl(b.getImageUrl()).build())
				.toList();

		return UserBookFullDto.builder().id(book.getId()).title(book.getTitle()).author(book.getAuthor())
				.category(book.getCategory()).coverImage(book.getCoverImagePath()).paid(book.isPaid())
				.price(book.getPrice()).contents(contents).build();
	}

	@Override
	public Map<String, List<BookSummaryDto>> getHomeBooks() {

		Map<String, Integer> categoryLimit = Map.of("HISTORY", 4, "POLITICS", 3, "TECHNOLOGY", 4, "LITERATURE", 2);

		Map<String, List<BookSummaryDto>> result = new LinkedHashMap<>();

		for (Map.Entry<String, Integer> entry : categoryLimit.entrySet()) {

			List<BookSummaryDto> books = bookRepo
					.findLatestByCategory(entry.getKey(), PageRequest.of(0, entry.getValue())).stream()
					.map(b -> BookSummaryDto.builder().id(b.getId()).title(b.getTitle()).author(b.getAuthor())
							.coverImage(b.getCoverImagePath()).build())
					.toList();

			result.put(entry.getKey(), books);
		}

		return result;
	}

}
