package com.digital.magazine.book.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.enums.HomeSectionConfig;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.book.service.UserBookService;
import com.digital.magazine.user.dto.BookSummaryDto;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional // 🔥 ADD THIS
public class UserBookServiceImpl implements UserBookService {

	private final BookRepository bookRepo;

	@Override
	public Map<String, List<BookSummaryDto>> getHomePage() {

		log.info("🏠 Building homepage");

		Map<String, List<BookSummaryDto>> response = new LinkedHashMap<>();

		for (HomeSectionConfig section : HomeSectionConfig.values()) {

			log.info("📌 Section | title={} | category={} | limit={}", section.getTitle(), section.getCategory(),
					section.getLimit());

			List<BookSummaryDto> books = bookRepo
					.findForHome(section.getCategory(), PageRequest.of(0, section.getLimit())).stream()
					.map(this::toSummary).toList();

			response.put(section.getTitle(), books);
		}

		return response;
	}

	private BookSummaryDto toSummary(Books b) {
		return BookSummaryDto.builder().id(b.getId()).title(b.getTitle()).subTitle(b.getSubtitle())
				.author(b.getAuthor()).category(b.getCategory().getTamilLabel()).coverImage(b.getCoverImagePath())
				.magazineNo(b.getMagazineNo()).status(b.getStatus())
				.uploadAt(b.getUpdatedAt() != null ? b.getUpdatedAt() : b.getCreatedAt()).build();
	}

}
