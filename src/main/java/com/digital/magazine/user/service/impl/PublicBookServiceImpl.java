package com.digital.magazine.user.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.common.enums.BookCategory;
import com.digital.magazine.common.enums.BookStatus;
import com.digital.magazine.user.dto.BookContentDto;
import com.digital.magazine.user.dto.BookSummaryDto;
import com.digital.magazine.user.dto.UserBookFullDto;
import com.digital.magazine.user.service.PublicBookService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional // 🔥 ADD THIS
public class PublicBookServiceImpl implements PublicBookService {

	private final BookRepository bookRepo;

//	@Override
//	public UserBookFullDto getFullBook(Long bookId) {
//
//		Books book = bookRepo.findById(bookId).filter(b -> b.getStatus() == BookStatus.PUBLISHED)
//				.orElseThrow(() -> new RuntimeException("Book not found"));
//
//		List<BookContentDto> contents = blockRepo.findByBookIdOrderByBlockOrder(bookId).stream()
//				.map(b -> BookContentDto.builder().order(b.getBlockOrder()).type(b.getType()).text(b.getTextContent())
//						.imageUrl(b.getImageUrl()).build())
//				.toList();
//
//		return UserBookFullDto.builder().id(book.getId()).title(book.getTitle()).author(book.getAuthor())
//				.category(book.getCategory().getTamilLabel()).coverImage(book.getCoverImagePath()).paid(book.isPaid())
//				.price(book.getPrice()).contents(contents).build();
//	}

	@Override
	public Map<String, List<BookSummaryDto>> getHomeBooks() {

		log.info("🏠 Fetching home page latest books");

		Map<String, Integer> categoryLimit = Map.of("வரலாறு", 1, "சமூகம்", 1, "இலக்கியம்", 1, "பண்பாடு", 1, "சூழலியல்",
				1, "தலையங்கம்", 1, "சினிமா", 1);

		Map<String, List<BookSummaryDto>> result = new LinkedHashMap<>();

		for (Map.Entry<String, Integer> entry : categoryLimit.entrySet()) {

			String tamilLabel = entry.getKey();
			int limit = entry.getValue();

			BookCategory categoryEnum = BookCategory.fromTamil(tamilLabel);

			List<BookSummaryDto> books = bookRepo.findLatestByCategory(categoryEnum, PageRequest.of(0, limit)).stream()
					.map(b -> BookSummaryDto.builder().id(b.getId()).title(b.getTitle()).subTitle(b.getSubtitle())
							.author(b.getAuthor()).category(b.getCategory().getTamilLabel())
							.coverImage(b.getCoverImagePath()).magazineNo(b.getMagazineNo()).status(b.getStatus())
							.build())
					.toList();

			result.put(tamilLabel, books);
		}

		log.info("✅ Home books prepared | categories={}", result.keySet());

		return result;
	}

}
