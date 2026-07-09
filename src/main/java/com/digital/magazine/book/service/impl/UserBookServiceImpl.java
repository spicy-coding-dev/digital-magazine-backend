package com.digital.magazine.book.service.impl;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.digital.magazine.book.dto.BookDetailsResponseDto;
import com.digital.magazine.book.dto.BookDetailsWithRelatedResponseDto;
import com.digital.magazine.book.dto.BookSummaryDto;
import com.digital.magazine.book.dto.MagazineDetailsResponseDto;
import com.digital.magazine.book.entity.BookContent;
import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.entity.Tag;
import com.digital.magazine.book.enums.HomeSectionConfig;
import com.digital.magazine.book.repository.BookContentRepository;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.book.service.UserBookService;
import com.digital.magazine.common.enums.BookCategory;
import com.digital.magazine.common.enums.BookStatus;
import com.digital.magazine.common.exception.BookNotPublishedException;
import com.digital.magazine.common.exception.BookNotPurchasableException;
import com.digital.magazine.common.exception.InvalidBookException;
import com.digital.magazine.common.exception.InvalidCategoryException;
import com.digital.magazine.common.exception.InvalidStatusException;
import com.digital.magazine.common.exception.NoBooksFoundException;
import com.digital.magazine.common.exception.UserNotFoundException;
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
	private final BookContentRepository bookContentRepo;
	private final AccessService accessService;
	private final UserSubscriptionRepository userSubscriptionRepo;
	private final MagazinePurchaseRepository magazinePurchaseRepo;

	@Value("${supabase.bucket.private}")
	private String privateBucketName;

	@Override
	public Map<String, List<BookSummaryDto>> getHomePage(Principal loginUser) {

		log.info("🏠 Building homepage");

		final User user = (loginUser == null) ? null
				: userRepo.findByEmail(loginUser.getName())
						.orElseThrow(() -> new UserNotFoundException("பயனர் கிடைக்கவில்லை"));

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

		System.out.println("login user " + loginUser);

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

//		int currentYear = LocalDate.now().getYear(); // 🔥 2026
//
//		return books.stream().map(book -> mapToSummaryWithYearRule(book, user, category, currentYear)).toList();

		// 🔄 Entity → DTO (with ACCESS CHECK)
		return books.stream().map(book -> mapToSummary(book, user)).toList();
	}

//	@Override
//	public BookDetailsWithRelatedResponseDto getBookDetails(Long bookId, Authentication auth) {
//
//		log.info("📘 Fetching book + related | bookId={}", bookId);
//
//		// 🔐 ACCESS VALIDATION
//		Books book = validateBookAccess(bookId, auth);
//
//		// 🧾 CONTENT (OPTIONAL)
//		Optional<BookContent> bookContentOpt = bookContentRepo.findByBookId(bookId);
//
//		if (bookContentOpt.isEmpty()) {
//			log.warn("⚠️ Content not added yet | bookId={}", bookId);
//		}
//
//		// 📦 MAIN BOOK DTO
//		BookDetailsResponseDto bookDto = mapToBookDetailsDto(book, bookContentOpt.orElse(null));
//
//		// 🔁 RELATED BOOKS
//		List<Books> relatedBooks = bookRepo.findTop5ByCategoryAndStatusAndIdNotOrderByUpdatedAtDesc(book.getCategory(),
//				BookStatus.PUBLISHED, bookId);
//
//		User user = auth == null ? null : userRepo.findByEmail(auth.getName()).orElse(null);
//
//		List<BookSummaryDto> relatedDtos = relatedBooks.stream().map(b -> mapToSummary(b, user)).toList();
//
//		return BookDetailsWithRelatedResponseDto.builder().book(bookDto).relatedBooks(relatedDtos).build();
//	}

//	private BookDetailsResponseDto mapToBookDetailsDto(Books book, BookContent content) {
//
//		LocalDateTime publishedAt = book.getUpdatedAt() != null ? book.getUpdatedAt() : book.getCreatedAt();
//
//		return BookDetailsResponseDto.builder().id(book.getId()).title(book.getTitle()).subtitle(book.getSubtitle())
//				.authorName(book.getAuthor()).magazineNo(book.getMagazineNo())
//				.content(content != null ? content.getContent() : null).publishedAt(publishedAt)
//				.status(book.getStatus().name()).tags(book.getTags().stream().map(Tag::getName).toList()).build();
//	}

	private BookSummaryDto mapToSummary(Books book, User user) {

		boolean accessible = accessService.canAccessBook(user, book);

		return BookSummaryDto.builder().id(book.getId()).title(book.getTitle()).subTitle(book.getSubtitle())
				.author(book.getAuthor()).category(book.getCategory().getTamilLabel())
				.coverImage(book.getCoverImagePath()).magazineNo(book.getMagazineNo()).paid(book.isPaid())
				.issueType(book.getIssueType())

				// 🔥 MAIN LOGIC
				.price(book.getPrice()).status(book.getStatus()).accessible(accessible)
				.uploadAt(book.getUpdatedAt() != null ? book.getUpdatedAt() : book.getCreatedAt())

				.build();
	}

//	private BookSummaryDto mapToSummaryWithYearRule(Books book, User user, BookCategory category, int currentYear) {
//
//		LocalDateTime publishedDate = book.getUpdatedAt() != null ? book.getUpdatedAt() : book.getCreatedAt();
//
//		int publishedYear = 0;
//
//		if (publishedDate != null) {
//			publishedYear = publishedDate.getYear();
//		}
//
//		MagazineIssueType issueType = null;
//
//		// 🔥 ONLY FOR MAGAZINE CATEGORY
//		if (category == BookCategory.MAGAZINE) {
//			if (publishedYear == currentYear) {
//				issueType = MagazineIssueType.LATEST;
//			} else {
//				issueType = MagazineIssueType.PREVIOUS;
//			}
//		}
//
//		boolean accessible = accessService.canAccessBook(user, book);
//
//		return BookSummaryDto.builder().id(book.getId()).title(book.getTitle()).subTitle(book.getSubtitle())
//				.author(book.getAuthor()).category(book.getCategory().getTamilLabel())
//				.coverImage(book.getCoverImagePath()).magazineNo(book.getMagazineNo()).paid(book.isPaid())
//
//				// 🔥 MAIN LOGIC
//				.price(book.getPrice()).status(book.getStatus()).accessible(accessible)
//				.uploadAt(book.getUpdatedAt() != null ? book.getUpdatedAt() : book.getCreatedAt()).issueType(issueType)
//				.build();
//	}

	@Override
	public BookDetailsWithRelatedResponseDto getBookDetails(Long bookId, Authentication auth) {

		Books book = getBook(bookId);

		validatePublished(book);

		// 🚫 Magazine should not open using Book Details API
		if (book.getCategory() == BookCategory.MAGAZINE) {

			log.warn("🚫 Invalid API Access | bookId={} | category=MAGAZINE", bookId);

			throw new InvalidBookException(
					"இந்த பதிவு ஒரு இதழ். இதழ்கள் தனிப்பட்ட இதழ் பக்கத்தில் மட்டுமே திறக்கப்படும்.");
		}

		// 👤 Logged in user
		User user = null;
		if (auth != null) {
			user = userRepo.findByEmail(auth.getName()).orElse(null);
		}

		// 💳 Digital Subscription
		boolean hasDigitalSub = false;

		if (user != null) {
			hasDigitalSub = userSubscriptionRepo.existsByUserAndPlan_TypeAndStatusAndEndDateAfter(user,
					SubscriptionType.DIGITAL, SubscriptionStatus.ACTIVE, LocalDate.now());
		}

		// 🛒 Individual Purchase
		boolean purchased = false;

		if (user != null) {
			purchased = magazinePurchaseRepo.existsByUserAndBook(user, book);
		}

		// 📖 Book Content
		BookContent content = bookContentRepo.findByBookId(bookId).orElse(null);

		// 📅 Published Date (Created Date)
		LocalDateTime publishedDate = book.getCreatedAt();

		LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);

		boolean olderThanOneYear = publishedDate.isBefore(oneYearAgo);

		log.info("========================================================");
		log.info("📘 BOOK ACCESS VALIDATION");
		log.info("📚 Book ID                 : {}", book.getId());
		log.info("📖 Published Date          : {}", publishedDate);
		log.info("📅 One Year Cutoff         : {}", oneYearAgo);
		log.info("🗓️ Older Than One Year    : {}", olderThanOneYear);
		log.info("💳 Digital Subscription    : {}", hasDigitalSub);
		log.info("🛒 Purchased Book          : {}", purchased);
		log.info("========================================================");

		String finalContent = null;

		if (content != null) {

			// ✅ Old Books -> Everyone Full Access
			if (olderThanOneYear) {

				log.info("✅ ACCESS GRANTED : OLD BOOK -> FULL CONTENT");

				finalContent = content.getContent();
			}

			// ✅ Active Digital Subscription
			else if (hasDigitalSub) {

				log.info("✅ ACCESS GRANTED : DIGITAL SUBSCRIPTION");

				finalContent = content.getContent();
			}

			// ✅ Purchased Book
			else if (purchased) {

				log.info("✅ ACCESS GRANTED : BOOK PURCHASED");

				finalContent = content.getContent();
			}

			// 🔒 Preview
			else {

				log.info("🔒 ACCESS LIMITED : PREVIEW ONLY (1500 letters)");

				finalContent = getPreview(content.getContent(), 1500);
			}

		} else {

			log.warn("⚠️ No content found for bookId={}", bookId);
		}

		boolean preview = !(olderThanOneYear || hasDigitalSub || purchased);

		BookDetailsResponseDto bookDto = mapToBookDetailsDto(book, finalContent, preview);

		log.info("🔍 Fetching Related Books | category={}", book.getCategory());

		List<Books> relatedBooks = bookRepo.findTop5ByCategoryAndStatusAndIdNotOrderByCreatedAtDesc(book.getCategory(),
				BookStatus.PUBLISHED, book.getId());

		log.info("📚 Related Books Count : {}", relatedBooks.size());

		List<BookSummaryDto> relatedDtos = relatedBooks.stream().map(b -> mapToSummary(b, auth)).toList();

		log.info("📤 Response Preview Mode : {}", preview);
		log.info("========================================================");

		return BookDetailsWithRelatedResponseDto.builder().book(bookDto).relatedBooks(relatedDtos).build();
	}

	@Override
	public MagazineDetailsResponseDto getMagazineDetails(Long magazineNo, Authentication auth) {

		log.info("📰 [MAGAZINE DETAILS] magazineNo={}", magazineNo);

		// 📖 Get Magazine
		Books magazine = getMagazine(magazineNo);

		// ✅ Validate Published
		validatePublished(magazine);

		// 🔒 Paid Magazine Access Check
		if (magazine.isPaid()) {

			User user = null;

			if (auth != null) {
				user = userRepo.findByEmail(auth.getName()).orElse(null);
			}

			boolean hasDigitalSub = false;
			boolean purchased = false;

			if (user != null) {

				hasDigitalSub = userSubscriptionRepo.existsByUserAndPlan_TypeAndStatusAndEndDateAfter(user,
						SubscriptionType.DIGITAL, SubscriptionStatus.ACTIVE, LocalDate.now());

				purchased = magazinePurchaseRepo.existsByUserAndBook(user, magazine);
			}

			boolean accessible = hasDigitalSub || purchased;

			log.info("💳 Magazine Paid        : {}", magazine.isPaid());
			log.info("✅ Magazine Accessible : {}", accessible);

			if (!accessible) {

				log.warn("🚫 Magazine Access Denied | magazineNo={}", magazineNo);

				throw new BookNotPurchasableException("இந்த இதழைப் படிக்க சந்தா அல்லது வாங்குதல் அவசியம்.");
			}
		}

		// 📚 Fetch Articles
		List<Books> articles = bookRepo.findMagazineArticles(magazineNo, BookCategory.MAGAZINE, BookStatus.PUBLISHED);

		log.info("📚 Total Articles Found : {}", articles.size());

		// 🔄 Entity -> DTO
		List<BookDetailsResponseDto> response = articles.stream().map(this::mapMagazineArticle).toList();

		log.info("✅ Magazine Response Prepared | magazineNo={} | articleCount={}", magazineNo, response.size());

		log.info("🔍 Fetching Related Books | category={}", magazine.getCategory());

		List<Books> relatedBooks = bookRepo.findTop5ByCategoryAndStatusAndIdNotOrderByCreatedAtDesc(
				magazine.getCategory(), BookStatus.PUBLISHED, magazine.getId());

		log.info("📚 Related Books Count : {}", relatedBooks.size());

		List<BookSummaryDto> relatedDtos = relatedBooks.stream().map(b -> mapToSummary(b, auth)).toList();

		return MagazineDetailsResponseDto.builder().magazineNo(magazineNo).title(magazine.getTitle())
				.coverImage(magazine.getCoverImagePath()).articles(response).relatedBooks(relatedDtos).build();
	}

	private BookSummaryDto mapToSummary(Books book, Authentication auth) {

		User user = null;

		if (auth != null) {
			user = userRepo.findByEmail(auth.getName()).orElse(null);
		}

		boolean accessible = true;

		// 🔒 Paid Book Access Check
		if (book.isPaid()) {

			boolean hasDigitalSub = false;
			boolean purchased = false;

			if (user != null) {

				hasDigitalSub = userSubscriptionRepo.existsByUserAndPlan_TypeAndStatusAndEndDateAfter(user,
						SubscriptionType.DIGITAL, SubscriptionStatus.ACTIVE, LocalDate.now());

				purchased = magazinePurchaseRepo.existsByUserAndBook(user, book);
			}

			accessible = hasDigitalSub || purchased;
		}

		return BookSummaryDto.builder().id(book.getId()).title(book.getTitle()).subTitle(book.getSubtitle())
				.author(book.getAuthor()).category(book.getCategory().getTamilLabel())
				.coverImage(book.getCoverImagePath()).magazineNo(book.getMagazineNo()).paid(book.isPaid())
				.price(book.getPrice()).status(book.getStatus()).accessible(accessible).issueType(book.getIssueType())
				.uploadAt(book.getCreatedAt()).build();
	}

	private BookDetailsResponseDto mapMagazineArticle(Books book) {

		BookContent content = bookContentRepo.findByBookId(book.getId()).orElse(null);

		return BookDetailsResponseDto.builder().id(book.getId()).title(book.getTitle()).subtitle(book.getSubtitle())
				.authorName(book.getAuthor()).magazineNo(book.getMagazineNo())
				.category(book.getCategory().getTamilLabel()).coverImage(book.getCoverImagePath())
				.content(content != null ? content.getContent() : null).publishedAt(book.getCreatedAt())
				.status(book.getStatus().name()).tags(book.getTags().stream().map(Tag::getName).toList()).build();
	}

	private BookDetailsResponseDto mapToBookDetailsDto(Books book, String content, boolean preview) {

		LocalDateTime publishedAt = book.getUpdatedAt() != null ? book.getUpdatedAt() : book.getCreatedAt();

		return BookDetailsResponseDto.builder().id(book.getId()).title(book.getTitle()).subtitle(book.getSubtitle())
				.authorName(book.getAuthor()).magazineNo(book.getMagazineNo())
				.category(book.getCategory().getTamilLabel()).coverImage(book.getCoverImagePath())
				.content(content != null ? content : null).publishedAt(publishedAt).status(book.getStatus().name())
				.preview(preview).subscriptionRequired(preview).tags(book.getTags().stream().map(Tag::getName).toList())
				.build();
	}

	private String getPreview(String html, int maxCharacters) {

		if (html == null || html.isBlank()) {
			return null;
		}

		Document doc = Jsoup.parseBodyFragment(html);

		String bodyHtml = doc.body().html();

		if (bodyHtml.length() <= maxCharacters) {
			return bodyHtml;
		}

		String preview = bodyHtml.substring(0, maxCharacters);

		return Jsoup.parseBodyFragment(preview).body().html();
	}

	private void validatePublished(Books book) {

		log.info("🔍 Validating Published Status | id={}", book.getId());

		if (book.getStatus() != BookStatus.PUBLISHED) {

			log.warn("⚠️ Not Published | id={}", book.getId());

			throw new BookNotPublishedException("இந்த புத்தகம் இன்னும் வெளியிடப்படவில்லை");
		}

		log.info("✅ Validation Success | id={}", book.getId());
	}

	private Books getBook(Long bookId) {

		log.info("📘 Fetching Book | bookId={}", bookId);

		return bookRepo.findById(bookId).orElseThrow(() -> {
			log.warn("❌ Book not found | bookId={}", bookId);
			return new NoBooksFoundException("புத்தகம் கிடைக்கவில்லை");
		});
	}

	private Books getMagazine(Long magazineNo) {

		log.info("📰 Fetching Magazine | magazineNo={}", magazineNo);

		return bookRepo.findByMagazineNoAndCategory(magazineNo, BookCategory.MAGAZINE).orElseThrow(() -> {
			log.warn("❌ Magazine not found | magazineNo={}", magazineNo);
			return new NoBooksFoundException("இதழ் கிடைக்கவில்லை");
		});
	}

//	private Books validateBookAccess(Long bookId, Authentication auth) {
//
//		log.info("🔍 Validating book access | bookId={}", bookId);
//
//		Books book = bookRepo.findById(bookId).orElseThrow(() -> new NoBooksFoundException("புத்தகம் கிடைக்கவில்லை"));
//
//		// ✅ Published check
//		if (book.getStatus() != BookStatus.PUBLISHED) {
//			log.warn("⚠️ Book not published | bookId={}", bookId);
//			throw new BookNotPublishedException("இந்த புத்தகம் இன்னும் வெளியிடப்படவில்லை");
//		}
//
//		// 🆓 Free book
//		if (!book.isPaid()) {
//			log.info("🆓 Free book | access granted | bookId={}", bookId);
//			return book;
//		}
//
//		// 🔐 Paid book
//		if (auth == null) {
//			log.warn("🚫 Anonymous access blocked | bookId={}", bookId);
//			throw new UnauthorizedAccessException("உள்நுழையாமல் இந்த புத்தகத்தை பார்க்க முடியாது");
//		}
//
//		User user = userRepo.findByEmail(auth.getName())
//				.orElseThrow(() -> new UserNotFoundException("பயனர் கிடைக்கவில்லை"));
//
//		boolean hasDigitalSub = userSubscriptionRepo.existsByUserAndPlan_TypeAndStatusAndEndDateAfter(user,
//				SubscriptionType.DIGITAL, SubscriptionStatus.ACTIVE, LocalDate.now());
//
//		boolean purchased = magazinePurchaseRepo.existsByUserAndBook(user, book);
//
//		if (!hasDigitalSub && !purchased) {
//			log.warn("🚫 Access denied | user={} | bookId={}", user.getEmail(), bookId);
//			throw new BookNotPurchasableException("இந்த புத்தகத்தை நீங்கள் வாங்கவில்லை");
//		}
//
//		log.info("✅ Paid book access approved | user={} | bookId={}", user.getEmail(), bookId);
//
//		return book;
//	}

}
