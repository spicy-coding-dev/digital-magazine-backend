package com.digital.magazine.admin.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.digital.magazine.admin.dto.BookUploadRequestDto;
import com.digital.magazine.admin.entity.Books;
import com.digital.magazine.admin.repository.BookContentBlockRepository;
import com.digital.magazine.admin.repository.BookRepository;
import com.digital.magazine.admin.service.BookService;
import com.digital.magazine.common.pdf.PdfPageImageExtractor;
import com.digital.magazine.common.storage.SupabaseStorageService;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

	private final BookRepository bookRepo;
	private final BookContentBlockRepository blockRepo;
	private final SupabaseStorageService fileService; // Supabase uploader
	private final UserRepository userRepository;

	@Override
	public void uploadBook(BookUploadRequestDto dto, MultipartFile coverImage, MultipartFile contentPdf,
			UserDetails userDetails) {

		log.info("📘 Book upload started by admin={}", userDetails.getUsername());

		// 1️⃣ Upload cover image
		String coverImageUrl;
		try {
			coverImageUrl = fileService.uploadFile(coverImage, "books/covers");
		} catch (Exception e) {
			log.error("❌ Cover image upload failed", e);
			throw new RuntimeException("Cover image upload failed");
		}

		String email = userDetails.getUsername();

		User admin = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Admin not found"));

		// 2️⃣ Save book
		Books book = Books.builder().title(dto.getTitle()).category(dto.getCategory()).author(dto.getAuthor()) // 👈
																												// backend
																												// decide
				.paid(dto.getPaid()).price(dto.getPaid() ? dto.getPrice() : null).status(dto.getStatus())
				.coverImagePath(coverImageUrl).createdBy(admin).createdAt(LocalDateTime.now()).build();

		bookRepo.save(book);

		log.info("✅ Book saved with id={}", book.getId());

		// 3️⃣ Extract PDF content
		extractPdfContent(book, contentPdf);

		log.info("🎉 Book upload completed successfully. bookId={}", book.getId());
	}

	// 🔥 PDF extraction
	private void extractPdfContent(Books book, MultipartFile pdf) {

		log.info("📄 PDF extraction started for bookId={}", book.getId());

		try {
			PdfPageImageExtractor extractor = new PdfPageImageExtractor(book, blockRepo, fileService);

			extractor.extract(pdf); // ✅ HERE IT IS CALLED

			log.info("✅ PDF extracted page-wise successfully");

		} catch (Exception e) {
			log.error("❌ PDF extraction failed", e);
			throw new RuntimeException("PDF extraction failed", e);
		}
	}

}
