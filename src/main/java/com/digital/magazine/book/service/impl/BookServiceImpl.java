package com.digital.magazine.book.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.digital.magazine.analytics.repository.ArticleViewRepository;
import com.digital.magazine.analytics.repository.GuestArticleViewRepository;
import com.digital.magazine.book.dto.BookStatusUpdateDto;
import com.digital.magazine.book.dto.BookUpdateRequestDto;
import com.digital.magazine.book.dto.BookUploadRequestDto;
import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.entity.Tag;
import com.digital.magazine.book.repository.BookContentBlockRepository;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.book.repository.TagRepository;
import com.digital.magazine.book.service.BookService;
import com.digital.magazine.common.enums.BookCategory;
import com.digital.magazine.common.enums.BookStatus;
import com.digital.magazine.common.exception.FileDeletionException;
import com.digital.magazine.common.exception.FileUploadException;
import com.digital.magazine.common.exception.InvalidCategoryException;
import com.digital.magazine.common.exception.InvalidFileException;
import com.digital.magazine.common.exception.InvalidStatusException;
import com.digital.magazine.common.exception.InvalidStatusTransitionException;
import com.digital.magazine.common.exception.NoBooksFoundException;
import com.digital.magazine.common.exception.UserNotFoundException;
import com.digital.magazine.common.pdf.PdfPageImageExtractor;
import com.digital.magazine.common.storage.SupabaseStorageService;
import com.digital.magazine.user.dto.BookSummaryDto;
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
	private final TagRepository tagRepo;
	private final SupabaseStorageService supabaseStorageService;
	private final GuestArticleViewRepository guestArticleRepo;
	private final ArticleViewRepository articleViewRepo;

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
			throw new FileUploadException("கவர் படத்தை பதிவேற்ற முடியவில்லை", e);
		}

		String email = userDetails.getUsername();

		User admin = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("Admin not found"));

		// 🔁 Tamil string → Enum conversion
		BookCategory category = BookCategory.fromTamil(dto.getCategory());

		Set<Tag> tagEntities = resolveTags(dto.getTags());

		// 2️⃣ Save book
		Books book = Books.builder().title(dto.getTitle()).subtitle(dto.getSubtitle()).category(category)
				.author(dto.getAuthor()).magazineNo(dto.getMagazineNo()).tags(tagEntities) // 👈
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

	@Override
	public List<BookSummaryDto> getBooksByCategory(String categoryLabel, String statusLabel) {

		log.info("📚 Fetch books by category | label={}", categoryLabel);

		// 🔁 Tamil → Enum conversion
		BookCategory category;
		try {
			category = BookCategory.fromTamil(categoryLabel);
			log.info("🔁 Category converted | Tamil={} → Enum={}", categoryLabel, category);
		} catch (Exception e) {
			log.error("❌ Invalid category received | label={}", categoryLabel);
			throw new InvalidCategoryException("தவறான புத்தக வகை வழங்கப்பட்டுள்ளது");
		}

		// 🔁 String → Enum (Status)
		BookStatus status;
		try {
			status = BookStatus.fromString(statusLabel);
			log.info("🔁 Status converted | String={} → Enum={}", statusLabel, status);
		} catch (Exception e) {
			log.error("❌ Invalid status | value={}", statusLabel);
			throw new InvalidStatusException("தவறான புத்தக நிலை வழங்கப்பட்டுள்ளது");
		}

		// 🗄️ DB fetch
		List<Books> books = bookRepo.findByCategoryAndStatus(category, status);

		if (books.isEmpty()) {
			log.warn("⚠️ No books found | category={}", category);

			throw new NoBooksFoundException("இந்த பிரிவில் தற்போது எந்த புத்தகங்களும் இல்லை");
		}

		log.info("🗄️ Books fetched from DB | category={}, count={}", category, books.size());

		// 🔄 Entity → DTO mapping
		List<BookSummaryDto> response = books.stream()
				.map(book -> BookSummaryDto.builder().id(book.getId()).title(book.getTitle())
						.subTitle(book.getSubtitle()).author(book.getAuthor())
						.category(book.getCategory().getTamilLabel()) // ✅ Tamil
						.coverImage(book.getCoverImagePath()).paid(book.isPaid()).magazineNo(book.getMagazineNo())
						.price(book.isPaid() ? book.getPrice() : null).status(book.getStatus())
						.tags(book.getTags().stream().map(Tag::getName) // 🔥 Entity → String
								.toList())
						.build())
				.toList();

		log.info("✅ Response DTO prepared | category={}, responseCount={}", categoryLabel, response.size());

		return response;
	}

	@Override
	public BookSummaryDto updateBook(Long bookId, BookUpdateRequestDto dto, Authentication auth) {

		userRepository.findByEmail(auth.getName()).orElseThrow(() -> new UserNotFoundException("Admin not found"));

		Books book = bookRepo.findById(bookId).orElseThrow(() -> new NoBooksFoundException("Book not found"));

		if (dto.getTitle() != null) {
			book.setTitle(dto.getTitle());
		}

		if (dto.getSubTitle() != null) {
			book.setSubtitle(dto.getSubTitle());
		}

		if (dto.getAuthor() != null) {
			book.setAuthor(dto.getAuthor());
		}

		if (dto.getCategory() != null) {
			book.setCategory(BookCategory.fromTamil(dto.getCategory()));
		}

		if (dto.getPaid() != null) {
			book.setPaid(dto.getPaid());
			if (Boolean.TRUE.equals(dto.getPaid())) {
				book.setPrice(dto.getPrice());
			} else {
				book.setPrice(null);
			}
		}

		if (dto.getPrice() != null && Boolean.TRUE.equals(book.isPaid())) {
			book.setPrice(dto.getPrice());
		}

		if (dto.getMagazineNo() != null) {
			book.setMagazineNo(dto.getMagazineNo());
		}

		if (dto.getTags() != null && !dto.getTags().isEmpty()) {
			book.getTags().clear();
			Set<Tag> tags = dto.getTags().stream().map(this::getOrCreateTag).collect(Collectors.toSet());
			book.getTags().addAll(tags);
		}

		book.setUpdatedAt(LocalDateTime.now());

		bookRepo.save(book);

		log.info("Book updated successfully | bookId={}", bookId);

		return mapToSummary(book);
	}

	private Tag getOrCreateTag(String name) {

		return tagRepo.findByName(name).orElseGet(() -> {
			Tag tag = Tag.builder().name(name).build();
			return tagRepo.save(tag);
		});
	}

	@Override
	public String updateCoverImage(Long bookId, MultipartFile file, Authentication auth) {

		// 🔐 Admin validate
		userRepository.findByEmail(auth.getName())
				.orElseThrow(() -> new UserNotFoundException("நிர்வாகி காணப்படவில்லை"));

		// 📘 Book fetch
		Books book = bookRepo.findById(bookId).orElseThrow(() -> new NoBooksFoundException("புத்தகம் கிடைக்கவில்லை"));

		// ❌ Empty file check
		if (file == null || file.isEmpty()) {
			log.warn("Empty cover image upload attempt | bookId={}", bookId);
			throw new InvalidFileException("கவர் படம் அவசியம்");
		}

		// 🧹 STEP 1: Delete old cover image
		String oldImageUrl = book.getCoverImagePath();
		if (oldImageUrl != null && !oldImageUrl.isBlank()) {
			log.info("🧹 Deleting old cover image | bookId={} | url={}", bookId, oldImageUrl);
			try {
				deleteSupabaseFileSafely(book.getCoverImagePath(), "Cover image", bookId);
			} catch (Exception e) {
				log.error("❌ Failed to delete old cover image | bookId={}", bookId, e);
				throw new FileDeletionException("பழைய கவர் படத்தை நீக்க முடியவில்லை", e);
			}
		}

		// 🔼 STEP 2: Upload new image
		String newImageUrl;
		try {
			newImageUrl = supabaseStorageService.uploadFile(file, "books/covers");
		} catch (Exception e) {
			log.error("❌ Failed to upload new cover image | bookId={}", bookId, e);
			throw new FileUploadException("புதிய கவர் படத்தை பதிவேற்ற முடியவில்லை", e);
		}

		// 🔄 STEP 3: Update book
		book.setCoverImagePath(newImageUrl);
		book.setUpdatedAt(LocalDateTime.now());
		bookRepo.save(book);

		log.info("✅ Cover image updated | bookId={} | newUrl={}", bookId, newImageUrl);

		return newImageUrl;
	}

	@Override
	public void changeStatus(Long bookId, BookStatusUpdateDto dto, Authentication auth) {

		User admin = userRepository.findByEmail(auth.getName())
				.orElseThrow(() -> new UserNotFoundException("நிர்வாகி காணப்படவில்லை"));

		Books book = bookRepo.findById(bookId).orElseThrow(() -> new NoBooksFoundException("புத்தகம் கிடைக்கவில்லை"));

		BookStatus oldStatus = book.getStatus();
		BookStatus newStatus = dto.getStatus();

		validateTransition(oldStatus, newStatus);

		book.setStatus(newStatus);
		book.setUpdatedAt(LocalDateTime.now());

		bookRepo.save(book);

		log.info("📘 Book status changed | bookId={} | {} → {} | by={}", bookId, oldStatus, newStatus,
				admin.getEmail());

		if (newStatus == BookStatus.BLOCKED) {
			log.warn("🚫 Book BLOCKED | bookId={} ", bookId);
		}
	}

	@Override
	public void deleteBook(Long bookId, Authentication auth) {

		User admin = userRepository.findByEmail(auth.getName())
				.orElseThrow(() -> new UserNotFoundException("Admin not found"));

		Books book = bookRepo.findById(bookId).orElseThrow(() -> new NoBooksFoundException("Book not found"));

		log.warn("⚠️ BOOK DELETE START | bookId={} | by={}", bookId, admin.getEmail());

		// 🧹 1️⃣ Delete analytics data FIRST
		log.info("🧹 Deleting guest article views | bookId={}", bookId);
		guestArticleRepo.deleteByArticleId(bookId);

		log.info("🧹 Deleting user article views | bookId={}", bookId);
		articleViewRepo.deleteByArticleId(bookId);

		// 🧹 2️⃣ Delete content block images
		blockRepo.findByBookId(bookId).forEach(block -> {
			if (block.getImageUrl() != null) {
				deleteSupabaseFileSafely(block.getImageUrl(), "Content image", bookId);
			}
		});

		// 🧹 3️⃣ Delete cover image
		deleteSupabaseFileSafely(book.getCoverImagePath(), "Cover image", bookId);

		// 🧹 4️⃣ Clear relations
		book.getTags().clear();
		book.getContentBlocks().clear();

		// 🗑️ 5️⃣ Delete book
		bookRepo.delete(book);

		log.info("✅ BOOK DELETE COMPLETED | bookId={}", bookId);
	}

	private void deleteSupabaseFileSafely(String fileUrl, String label, Long bookId) {

		if (fileUrl == null || fileUrl.isBlank()) {
			return;
		}

		try {
			log.info("🧹 Deleting {} from Supabase | bookId={} | url={}", label, bookId, fileUrl);

			supabaseStorageService.deleteFileFromSupabase(fileUrl);

		} catch (Exception e) {
			log.error("❌ Failed to delete {} | bookId={} | url={}", label, bookId, fileUrl, e);

			throw new FileDeletionException("Supabase-ல் உள்ள கோப்புகளை நீக்க முடியவில்லை");
		}
	}

	// 🔐 STATUS RULE VALIDATION (Tamil messages)
	private void validateTransition(BookStatus from, BookStatus to) {

		if (from == BookStatus.DRAFT && to == BookStatus.DRAFT) {
			throw new InvalidStatusTransitionException("இந்த புத்தகம் ஏற்கனவே வரைவு நிலையில் (DRAFT) உள்ளது");
		}

		if (from == BookStatus.PUBLISHED && to == BookStatus.PUBLISHED) {
			throw new InvalidStatusTransitionException(
					"இந்த புத்தகம் ஏற்கனவே வெளியிடப்பட்ட நிலையில் (PUBLISHED) உள்ளது");
		}

		if (from == BookStatus.BLOCKED && to == BookStatus.BLOCKED) {
			throw new InvalidStatusTransitionException("இந்த புத்தகம் ஏற்கனவே தடுக்கப்பட்ட நிலையில் (BLOCKED) உள்ளது");
		}

		if (from == BookStatus.BLOCKED && to == BookStatus.DRAFT) {
			throw new InvalidStatusTransitionException(
					"தடுக்கப்பட்ட புத்தகத்தை நேரடியாக வரைவு நிலைக்கு மாற்ற முடியாது");
		}
	}

	private BookSummaryDto mapToSummary(Books book) {

		return BookSummaryDto.builder().id(book.getId()).title(book.getTitle()).subTitle(book.getSubtitle())
				.author(book.getAuthor()).category(book.getCategory().getTamilLabel())
				.coverImage(book.getCoverImagePath()).magazineNo(book.getMagazineNo()).paid(book.isPaid())
				.price(book.isPaid() ? book.getPrice() : null).status(book.getStatus())

				// 🔥 IMPORTANT CHANGE – TAG ENTITY → STRING LIST
				.tags(book.getTags().stream().map(Tag::getName).toList())

				.build();
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

	private Set<Tag> resolveTags(List<String> tags) {

		Set<Tag> tagSet = new HashSet<>();

		for (String rawTag : tags) {

			String tagName = rawTag.trim().toLowerCase();

			Tag tag = tagRepo.findByNameIgnoreCase(tagName)
					.orElseGet(() -> tagRepo.save(Tag.builder().name(tagName).build()));

			tagSet.add(tag);
		}

		return tagSet;
	}

}
