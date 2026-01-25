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
	private final UserRepository userRepository;
	private final TagRepository tagRepo;
	private final SupabaseStorageService supabaseStorageService;
	private final GuestArticleViewRepository guestArticleRepo;
	private final ArticleViewRepository articleViewRepo;

	@Override
	public void uploadBook(BookUploadRequestDto dto, MultipartFile coverImage, MultipartFile contentPdf,
			UserDetails userDetails) {

		String adminEmail = userDetails.getUsername();
		log.info("📘 [BOOK UPLOAD START] admin={}", adminEmail);

		User admin = userRepository.findByEmail(adminEmail).orElseThrow(() -> {
			log.error("❌ Admin not found email={}", adminEmail);
			return new UserNotFoundException("Admin not found");
		});

		// 1️⃣ Cover Image
		log.info("🖼️ Uploading cover image...");
		String coverImageUrl = supabaseStorageService.uploadPublicFile(coverImage, "books/covers");

		// 2️⃣ PDF
		log.info("📄 Uploading book PDF...");
		String pdfPath = supabaseStorageService.uploadPrivateFile(contentPdf, "books/pdfs");

		BookCategory category = BookCategory.fromTamil(dto.getCategory());
		Set<Tag> tags = resolveTags(dto.getTags());

		Books book = Books.builder().title(dto.getTitle()).subtitle(dto.getSubtitle()).author(dto.getAuthor())
				.magazineNo(dto.getMagazineNo()).category(category).tags(tags).paid(dto.getPaid())
				.price(dto.getPaid() ? dto.getPrice() : null).status(dto.getStatus()).coverImagePath(coverImageUrl)
				.pdfPath(pdfPath).createdBy(admin).createdAt(LocalDateTime.now()).build();

		bookRepo.save(book);

		log.info("🎉 [BOOK UPLOAD SUCCESS] bookId={} admin={}", book.getId(), adminEmail);
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

		String adminEmail = auth.getName();

		log.info("🖼️ [COVER UPDATE START] bookId={} by={}", bookId, adminEmail);

		// 🔐 1️⃣ Admin validation
		userRepository.findByEmail(adminEmail).orElseThrow(() -> {
			log.error("❌ Admin not found | email={}", adminEmail);
			return new UserNotFoundException("நிர்வாகி காணப்படவில்லை");
		});

		// 📘 2️⃣ Fetch book
		Books book = bookRepo.findById(bookId).orElseThrow(() -> {
			log.error("❌ Book not found | bookId={}", bookId);
			return new NoBooksFoundException("புத்தகம் கிடைக்கவில்லை");
		});

		// ❌ 3️⃣ Empty file check
		if (file == null || file.isEmpty()) {
			log.warn("⚠️ Empty cover image upload | bookId={}", bookId);
			throw new InvalidFileException("கவர் படம் அவசியம்");
		}

		// 🧹 4️⃣ Delete old cover image (PUBLIC BUCKET)
		String oldImageUrl = book.getCoverImagePath();
		if (oldImageUrl != null && !oldImageUrl.isBlank()) {
			log.info("🧹 [OLD COVER DELETE] bookId={} url={}", bookId, oldImageUrl);
			try {
				supabaseStorageService.deletePublicFile(oldImageUrl);
			} catch (Exception e) {
				log.error("❌ Old cover delete failed | bookId={}", bookId, e);
				throw new FileDeletionException("பழைய கவர் படத்தை நீக்க முடியவில்லை", e);
			}
		}

		// 🔼 5️⃣ Upload new cover image (PUBLIC BUCKET)
		String newImageUrl;
		try {
			log.info("📤 Uploading new cover image | bookId={}", bookId);
			newImageUrl = supabaseStorageService.uploadPublicFile(file, "books/covers");
		} catch (Exception e) {
			log.error("❌ New cover upload failed | bookId={}", bookId, e);
			throw new FileUploadException("புதிய கவர் படத்தை பதிவேற்ற முடியவில்லை", e);
		}

		// 🔄 6️⃣ Update DB
		book.setCoverImagePath(newImageUrl);
		book.setUpdatedAt(LocalDateTime.now());
		bookRepo.save(book);

		log.info("✅ [COVER UPDATE SUCCESS] bookId={} newUrl={}", bookId, newImageUrl);

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

		log.warn("⚠️ [BOOK DELETE START] bookId={} by={}", bookId, admin.getEmail());

		// 1️⃣ Analytics cleanup
		log.info("🧹 Deleting guest views | bookId={}", bookId);
		guestArticleRepo.deleteByArticleId(bookId);

		log.info("🧹 Deleting user views | bookId={}", bookId);
		articleViewRepo.deleteByArticleId(bookId);

		// ❌ OLD content blocks logic REMOVE (no OCR blocks anymore)

		// 2️⃣ Delete COVER image (PUBLIC)
		supabaseStorageService.deletePublicFile(book.getCoverImagePath());

		// 3️⃣ Delete PDF (PRIVATE) 🔥
		supabaseStorageService.deletePrivateFile(book.getPdfPath());

		// 4️⃣ Clear relations
		book.getTags().clear();

		// 5️⃣ Delete book record
		bookRepo.delete(book);

		log.info("✅ [BOOK DELETE COMPLETED] bookId={}", bookId);
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
