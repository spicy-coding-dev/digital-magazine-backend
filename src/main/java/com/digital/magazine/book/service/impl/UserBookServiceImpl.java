package com.digital.magazine.book.service.impl;

import java.security.Principal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.digital.magazine.book.dto.BookSummaryDto;
import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.entity.Tag;
import com.digital.magazine.book.enums.HomeSectionConfig;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.book.service.UserBookService;
import com.digital.magazine.common.enums.BookCategory;
import com.digital.magazine.common.enums.BookStatus;
import com.digital.magazine.common.exception.InvalidCategoryException;
import com.digital.magazine.common.exception.InvalidStatusException;
import com.digital.magazine.common.exception.NoBooksFoundException;
import com.digital.magazine.common.exception.UserNotFoundException;
import com.digital.magazine.common.storage.SupabaseStorageService;
import com.digital.magazine.subscription.enums.SubscriptionStatus;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.subscription.repository.MagazinePurchaseRepository;
import com.digital.magazine.subscription.repository.UserSubscriptionRepository;
import com.digital.magazine.subscription.service.AccessService;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional // 🔥 ADD THIS
public class UserBookServiceImpl implements UserBookService {

	private final UserRepository userRepo;
	private final BookRepository bookRepo;
	private final AccessService accessService;
	private final SupabaseStorageService supabaseStorage;
	private final UserSubscriptionRepository userSubscriptionRepo;
	private final MagazinePurchaseRepository magazinePurchaseRepo;
	@Value("${supabase.bucket.private}")
	private String privateBucketName;

	@Override
	public Map<String, List<BookSummaryDto>> getHomePage(Principal loginUser) {

		log.info("🏠 Building homepage");

		final User user = (loginUser == null) ? null
				: userRepo.findByEmail(loginUser.getName())
						.orElseThrow(() -> new UserNotFoundException("User not found"));

		Map<String, List<BookSummaryDto>> response = new LinkedHashMap<>();

		for (HomeSectionConfig section : HomeSectionConfig.values()) {

			log.info("📌 Section | title={} | category={} | limit={}", section.getTitle(), section.getCategory(),
					section.getLimit());

			List<BookSummaryDto> books = bookRepo
					.findForHome(section.getCategory(), PageRequest.of(0, section.getLimit())).stream()
					.map(book -> mapToSummary(book, user)) // 🔥 FIX HERE
					.toList();

			response.put(section.getTitle(), books);
		}

		return response;
	}

	@Override
	public List<BookSummaryDto> getBooksByCategory(String categoryLabel, String statusLabel, Principal loginUser) {

		log.info("📚 Fetch books by category | label={}", categoryLabel);

		final User user = (loginUser == null) ? null
				: userRepo.findByEmail(loginUser.getName())
						.orElseThrow(() -> new UserNotFoundException("User not found"));

		// 🔁 Tamil → Enum (Category)
		BookCategory category;
		try {
			category = BookCategory.fromTamil(categoryLabel);
		} catch (Exception e) {
			throw new InvalidCategoryException("தவறான புத்தக வகை வழங்கப்பட்டுள்ளது");
		}

		// 🔁 String → Enum (Status)
		BookStatus status;
		try {
			status = BookStatus.fromString(statusLabel);
		} catch (Exception e) {
			throw new InvalidStatusException("தவறான புத்தக நிலை வழங்கப்பட்டுள்ளது");
		}

		// 🗄️ DB fetch
		List<Books> books = bookRepo.findByCategoryAndStatus(category, status);

		if (books.isEmpty()) {
			throw new NoBooksFoundException("இந்த பிரிவில் தற்போது எந்த புத்தகங்களும் இல்லை");
		}

		// 🔄 Entity → DTO (with ACCESS CHECK)
		return books.stream().map(book -> mapToSummary(book, user)).toList();
	}

	private BookSummaryDto mapToSummary(Books book, User user) {

		boolean accessible = accessService.canAccessBook(user, book);

		return BookSummaryDto.builder().id(book.getId()).title(book.getTitle()).subTitle(book.getSubtitle())
				.author(book.getAuthor()).category(book.getCategory().getTamilLabel())
				.coverImage(book.getCoverImagePath()).magazineNo(book.getMagazineNo()).paid(book.isPaid())

				// 🔥 MAIN LOGIC
				.price(accessible ? book.getPrice() : null).status(book.getStatus()).accessible(accessible)

				// 🔥 TAGS
				.tags(book.getTags().stream().map(Tag::getName).toList())

				.build();
	}

	@Override
	public String getBookContentPdf(Long bookId, Authentication auth) {

		log.info("🔍 Fetching book content | bookId={}", bookId);

		Books book = bookRepo.findById(bookId).orElseThrow(() -> new RuntimeException("புத்தகம் கிடைக்கவில்லை"));

		if (book.getStatus() != BookStatus.PUBLISHED) {
			log.warn("❌ Book not published | bookId={}", bookId);
			throw new IllegalStateException("இந்த புத்தகம் இன்னும் வெளியிடப்படவில்லை");
		}

		// 🟢 FREE BOOK → allow
		if (!book.isPaid()) {
			log.info("🆓 Free book access | bookId={}", bookId);
			return generateSignedUrl(book);
		}

		// 🔐 PAID BOOK → user check
		if (auth == null) {
			log.warn("❌ Anonymous access blocked | bookId={}", bookId);
			throw new IllegalStateException("உள்நுழையாமல் இந்த புத்தகத்தை பார்க்க முடியாது");
		}

		User user = userRepo.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));

		// ✅ DIGITAL SUBSCRIPTION
		boolean hasDigitalSub = userSubscriptionRepo.existsByUserAndPlan_TypeAndStatusAndEndDateAfter(user,
				SubscriptionType.DIGITAL, SubscriptionStatus.ACTIVE, LocalDate.now());

		// ✅ SINGLE PURCHASE
		boolean purchased = magazinePurchaseRepo.existsByUserAndBook(user, book);

		if (!hasDigitalSub && !purchased) {
			log.warn("❌ Access denied | user={} | bookId={}", user.getEmail(), bookId);
			throw new IllegalStateException("இந்த புத்தகத்தை நீங்கள் வாங்கவில்லை");
		}

		log.info("✅ Paid book access granted | user={} | bookId={}", user.getEmail(), bookId);

		return generateSignedUrl(book);
	}

	private String generateSignedUrl(Books book) {

		String pdfPath = book.getPdfPath(); // stored in DB

		log.info("🔐 Generating signed URL | path={}", pdfPath);

		String signedUrl = supabaseStorage.generateSignedUrlFromPrivate(privateBucketName, pdfPath, 10 // minutes
																										// validity
		);

		log.info("🔗 Signed URL generated");

		return signedUrl;
	}

}
