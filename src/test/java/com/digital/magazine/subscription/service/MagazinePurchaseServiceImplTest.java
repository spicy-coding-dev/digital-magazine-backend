package com.digital.magazine.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.common.enums.BookStatus;
import com.digital.magazine.common.exception.*;
import com.digital.magazine.subscription.entity.MagazinePurchase;
import com.digital.magazine.subscription.entity.SubscriptionPlan;
import com.digital.magazine.subscription.entity.UserSubscription;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.subscription.repository.MagazinePurchaseRepository;
import com.digital.magazine.subscription.repository.UserSubscriptionRepository;
import com.digital.magazine.subscription.service.impl.MagazinePurchaseServiceImpl;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class MagazinePurchaseServiceImplTest {

	@Mock
	private MagazinePurchaseRepository purchaseRepo;

	@Mock
	private UserRepository userRepo;

	@Mock
	private BookRepository bookRepo;

	@Mock
	private UserSubscriptionRepository userSubscriptionRepo;

	@Mock
	private Authentication authentication;

	@InjectMocks
	private MagazinePurchaseServiceImpl service;

	private User user;
	private Books book;

	@BeforeEach
	void setup() {
		user = User.builder().id(1L).email("user@test.com").name("Test User").build();

		book = Books.builder().id(10L).title("Test Magazine").magazineNo(2l).price(70.00).paid(true)
				.status(BookStatus.PUBLISHED).build();

		when(authentication.getName()).thenReturn("user@test.com");
	}

	// ✅ SUCCESS
	@Test
	void purchase_success() {

		when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));
		when(bookRepo.findById(10L)).thenReturn(Optional.of(book));
		when(userSubscriptionRepo.findActiveByUser(user)).thenReturn(Optional.empty());
		when(purchaseRepo.existsByUserAndBook(user, book)).thenReturn(false);

		String result = service.purchase(authentication, 10L);

		assertTrue(result.contains("வெற்றிகரமாக"));
		verify(purchaseRepo).save(any(MagazinePurchase.class));
	}

	// ❌ FREE BOOK
	@Test
	void purchase_freeBook_shouldThrowException() {

		book.setPaid(false);

		when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));
		when(bookRepo.findById(10L)).thenReturn(Optional.of(book));

		assertThrows(FreeBookException.class, () -> service.purchase(authentication, 10L));
	}

	// ❌ BOOK NOT PURCHASABLE
	@Test
	void purchase_blockedBook_shouldThrowException() {

		book.setStatus(BookStatus.BLOCKED);

		when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));
		when(bookRepo.findById(10L)).thenReturn(Optional.of(book));

		assertThrows(BookNotPurchasableException.class, () -> service.purchase(authentication, 10L));
	}

	// ❌ DIGITAL SUBSCRIPTION EXISTS
	@Test
	void purchase_digitalSubscriptionExists_shouldThrowException() {

		SubscriptionPlan plan = SubscriptionPlan.builder().type(SubscriptionType.DIGITAL).build();

		UserSubscription sub = UserSubscription.builder().plan(plan).build();

		when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));
		when(bookRepo.findById(10L)).thenReturn(Optional.of(book));
		when(userSubscriptionRepo.findActiveByUser(user)).thenReturn(Optional.of(sub));

		assertThrows(DigitalSubscriptionExistsException.class, () -> service.purchase(authentication, 10L));
	}

	// ❌ ALREADY PURCHASED
	@Test
	void purchase_alreadyPurchased_shouldThrowException() {

		when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));
		when(bookRepo.findById(10L)).thenReturn(Optional.of(book));
		when(userSubscriptionRepo.findActiveByUser(user)).thenReturn(Optional.empty());
		when(purchaseRepo.existsByUserAndBook(user, book)).thenReturn(true);

		assertThrows(AlreadyPurchasedException.class, () -> service.purchase(authentication, 10L));
	}

	// ❌ USER NOT FOUND
	@Test
	void purchase_userNotFound_shouldThrowException() {

		when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> service.purchase(authentication, 10L));
	}

	// ❌ BOOK NOT FOUND
	@Test
	void purchase_bookNotFound_shouldThrowException() {

		when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));
		when(bookRepo.findById(anyLong())).thenReturn(Optional.empty());

		assertThrows(NoBooksFoundException.class, () -> service.purchase(authentication, 10L));
	}
}
