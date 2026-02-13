package com.digital.magazine.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.subscription.enums.SubscriptionStatus;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.subscription.repository.MagazinePurchaseRepository;
import com.digital.magazine.subscription.repository.UserSubscriptionRepository;
import com.digital.magazine.subscription.service.impl.AccessServiceImpl;
import com.digital.magazine.user.entity.User;

@ExtendWith(MockitoExtension.class)
class AccessServiceImplTest {

	@Mock
	private UserSubscriptionRepository userSubscriptionRepo;

	@Mock
	private MagazinePurchaseRepository magazinePurchaseRepo;

	@InjectMocks
	private AccessServiceImpl accessService;

	private User user;
	private Books paidBook;
	private Books freeBook;

	@BeforeEach
	void setup() {
		user = User.builder().id(1L).name("Test User").build();

		paidBook = Books.builder().id(10L).paid(true).build();
		freeBook = Books.builder().id(20L).paid(false).build();
	}

	// ✅ Free book – always accessible
	@Test
	void canAccessBook_freeBook_shouldReturnTrue() {

		boolean result = accessService.canAccessBook(null, freeBook);

		assertTrue(result);
		verifyNoInteractions(userSubscriptionRepo, magazinePurchaseRepo);
	}

	// 🚫 Paid book + guest user
	@Test
	void canAccessBook_paidBook_guestUser_shouldReturnFalse() {

		boolean result = accessService.canAccessBook(null, paidBook);

		assertFalse(result);
	}

	// 🔥 Paid book + DIGITAL subscription
	@Test
	void canAccessBook_digitalSubscription_shouldReturnTrue() {

		when(userSubscriptionRepo.existsByUser_IdAndPlan_TypeAndStatus(1L, SubscriptionType.DIGITAL,
				SubscriptionStatus.ACTIVE)).thenReturn(true);

		boolean result = accessService.canAccessBook(user, paidBook);

		assertTrue(result);
		verify(userSubscriptionRepo).existsByUser_IdAndPlan_TypeAndStatus(1L, SubscriptionType.DIGITAL,
				SubscriptionStatus.ACTIVE);
	}

	// 🛒 Paid book + individual purchase
	@Test
	void canAccessBook_individualPurchase_shouldReturnTrue() {

		when(userSubscriptionRepo.existsByUser_IdAndPlan_TypeAndStatus(1L, SubscriptionType.DIGITAL,
				SubscriptionStatus.ACTIVE)).thenReturn(false);

		when(magazinePurchaseRepo.findBookIdsByUserId(1L)).thenReturn(Set.of(10L));

		boolean result = accessService.canAccessBook(user, paidBook);

		assertTrue(result);
		verify(magazinePurchaseRepo).findBookIdsByUserId(1L);
	}

	// ❌ Paid book + no subscription + no purchase
	@Test
	void canAccessBook_noAccess_shouldReturnFalse() {

		when(userSubscriptionRepo.existsByUser_IdAndPlan_TypeAndStatus(1L, SubscriptionType.DIGITAL,
				SubscriptionStatus.ACTIVE)).thenReturn(false);

		when(magazinePurchaseRepo.findBookIdsByUserId(1L)).thenReturn(Set.of());

		boolean result = accessService.canAccessBook(user, paidBook);

		assertFalse(result);
	}
}
