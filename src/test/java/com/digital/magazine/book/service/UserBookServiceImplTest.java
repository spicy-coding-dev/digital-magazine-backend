package com.digital.magazine.book.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;

import com.digital.magazine.book.dto.BookDetailsWithRelatedResponseDto;
import com.digital.magazine.book.dto.BookSummaryDto;
import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.repository.BookContentRepository;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.book.service.impl.UserBookServiceImpl;
import com.digital.magazine.common.enums.BookCategory;
import com.digital.magazine.common.enums.BookStatus;
import com.digital.magazine.common.exception.BookNotPublishedException;
import com.digital.magazine.subscription.repository.MagazinePurchaseRepository;
import com.digital.magazine.subscription.repository.UserSubscriptionRepository;
import com.digital.magazine.subscription.service.AccessService;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserBookServiceImplTest {

	@InjectMocks
	private UserBookServiceImpl service;

	@Mock
	private UserRepository userRepo;
	@Mock
	private BookRepository bookRepo;
	@Mock
	private BookContentRepository bookContentRepo;
	@Mock
	private AccessService accessService;
	@Mock
	private UserSubscriptionRepository userSubscriptionRepo;
	@Mock
	private MagazinePurchaseRepository magazinePurchaseRepo;
	@Mock
	private Authentication auth;

	private User user;
	private Books book;

	@BeforeEach
	void setup() {

		user = User.builder().id(1L).email("user@test.com").build();

		book = Books.builder().id(10L).title("Tamil History").author("Author").category(BookCategory.HISTORY)
				.status(BookStatus.PUBLISHED).paid(false).createdAt(LocalDateTime.now()).build();
	}

	// ✅ HOME PAGE
	@Test
	void getHomePage_success() {

		when(bookRepo.findForHome(any(), any(PageRequest.class))).thenReturn(List.of(book));

		when(accessService.canAccessBook(null, book)).thenReturn(true);

		Map<String, List<BookSummaryDto>> result = service.getHomePage(null);

		assertFalse(result.isEmpty());
	}

	// ✅ CATEGORY LIST
	@Test
	void getBooksByCategory_success() {

		when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.of(user));

		when(bookRepo.findByCategoryAndStatus(BookCategory.HISTORY, BookStatus.PUBLISHED)).thenReturn(List.of(book));

		when(accessService.canAccessBook(user, book)).thenReturn(true);

		List<BookSummaryDto> result = service.getBooksByCategory("வரலாறு", "PUBLISHED", () -> "user@test.com");

		assertEquals(1, result.size());
		assertTrue(result.get(0).isAccessible());
	}

	// ❌ BOOK NOT PUBLISHED
	@Test
	void getBookDetails_notPublished() {

		book.setStatus(BookStatus.DRAFT);

		when(bookRepo.findById(10L)).thenReturn(Optional.of(book));

		assertThrows(BookNotPublishedException.class, () -> service.getBookDetails(10L, auth));
	}

	// ✅ FREE BOOK ACCESS
	@Test
	void getBookDetails_freeBook_success() {

		when(bookRepo.findById(10L)).thenReturn(Optional.of(book));

		BookDetailsWithRelatedResponseDto response = service.getBookDetails(10L, null);

		assertNotNull(response.getBook());
		verify(bookRepo).findById(10L);
	}
}
