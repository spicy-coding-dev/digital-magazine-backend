package com.digital.magazine.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.subscription.dto.MagazinePurchaseAdminDto;
import com.digital.magazine.subscription.dto.SubscribedUserDto;
import com.digital.magazine.subscription.entity.MagazinePurchase;
import com.digital.magazine.subscription.entity.SubscriptionPlan;
import com.digital.magazine.subscription.entity.UserAddress;
import com.digital.magazine.subscription.entity.UserSubscription;
import com.digital.magazine.subscription.enums.SubscriptionStatus;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.subscription.repository.MagazinePurchaseRepository;
import com.digital.magazine.subscription.repository.UserSubscriptionRepository;
import com.digital.magazine.subscription.service.impl.SubscriptionQueryServiceImpl;
import com.digital.magazine.user.entity.User;

@ExtendWith(MockitoExtension.class)
class SubscriptionQueryServiceImplTest {

	@Mock
	private UserSubscriptionRepository subscriptionRepo;
	@Mock
	private MagazinePurchaseRepository purchaseRepo;
	@Mock
	private BookRepository bookRepo;

	@InjectMocks
	private SubscriptionQueryServiceImpl service;

	/* ================= getSubscribedUsers ================= */

	@Test
	void getSubscribedUsers_print_withAddress() {

		User user = User.builder().id(1L).name("User").email("u@test.com").build();

		UserAddress address = UserAddress.builder().name("Home").addressLine("Street").city("Chennai").state("TN")
				.pincode("600001").mobile("9999999999").build();

		SubscriptionPlan plan = SubscriptionPlan.builder().name("Print 1Y").type(SubscriptionType.PRINT).build();

		UserSubscription sub = UserSubscription.builder().id(10L).user(user).plan(plan).deliveryAddress(address)
				.startDate(LocalDate.now()).endDate(LocalDate.now().plusYears(1)).status(SubscriptionStatus.ACTIVE)
				.build();

		when(subscriptionRepo.findByPlan_TypeAndStatus(SubscriptionType.PRINT, SubscriptionStatus.ACTIVE))
				.thenReturn(List.of(sub));

		List<SubscribedUserDto> result = service.getSubscribedUsers(SubscriptionType.PRINT, SubscriptionStatus.ACTIVE);

		assertEquals(1, result.size());
		assertNotNull(result.get(0).getAddress());
		assertEquals("User", result.get(0).getName());
	}

	@Test
	void getSubscribedUsers_allTypes_success() {

		// 👤 User
		User user = User.builder().id(1L).name("User One").email("user@test.com").build();

		// 📦 Subscription Plan (IMPORTANT)
		SubscriptionPlan plan = SubscriptionPlan.builder().id(10L).name("Print Plan").type(SubscriptionType.PRINT)
				.durationYears(1).build();

		// 🏠 Address (optional but safe)
		UserAddress address = UserAddress.builder().name("User One").addressLine("Street").city("Chennai").state("TN")
				.pincode("600001").mobile("9999999999").build();

		// 🧾 Subscription (FULL OBJECT)
		UserSubscription sub = UserSubscription.builder().id(100L).user(user).plan(plan) // 🔥 THIS WAS MISSING
				.deliveryAddress(address).startDate(LocalDate.now()).endDate(LocalDate.now().plusYears(1))
				.status(SubscriptionStatus.ACTIVE).build();

		when(subscriptionRepo.findByStatus(SubscriptionStatus.ACTIVE)).thenReturn(List.of(sub));

		List<SubscribedUserDto> result = service.getSubscribedUsers(null, SubscriptionStatus.ACTIVE);

		assertEquals(1, result.size());
		assertEquals("User One", result.get(0).getName());
		assertEquals("Print Plan", result.get(0).getPlanName());
	}

	/* ================= Purchases ================= */

	@Test
	void getAllPurchases_success() {

		User user = User.builder().id(1L).name("User").email("user@test.com").mobile("9999999999").build();

		Books book = Books.builder().id(10L).title("Magazine").magazineNo(2l).build();

		MagazinePurchase purchase = MagazinePurchase.builder().id(100L).user(user).book(book).price(70d)
				.purchasedAt(LocalDateTime.now()).build();

		when(purchaseRepo.findAllByOrderByPurchasedAtDesc()).thenReturn(List.of(purchase));

		List<MagazinePurchaseAdminDto> result = service.getAllPurchases();

		assertEquals(1, result.size());
		assertEquals("User", result.get(0).getUserName());
		assertEquals("Magazine", result.get(0).getBookTitle());
	}

	@Test
	void getPurchasesByBook_success() {

		User user = User.builder().id(1L).name("User").email("u@test.com").mobile("999").build();

		Books book = Books.builder().id(5L).title("History Mag").magazineNo(2l).build();

		MagazinePurchase purchase = MagazinePurchase.builder().id(1L).user(user).book(book).price(70.00)
				.purchasedAt(LocalDateTime.now()).build();

		when(bookRepo.findById(5L)).thenReturn(Optional.of(book));
		when(purchaseRepo.findByBookOrderByPurchasedAtDesc(book)).thenReturn(List.of(purchase));

		List<MagazinePurchaseAdminDto> result = service.getPurchasesByBook(5L);

		assertEquals(1, result.size());
		assertEquals("History Mag", result.get(0).getBookTitle());
	}

	@Test
	void getPurchasesBetweenDates_withoutBook() {

		// 👤 User (MUST)
		User user = User.builder().id(1L).name("User One").email("user@test.com").mobile("9999999999").build();

		// 📘 Book (MUST)
		Books book = Books.builder().id(10L).title("Magazine Jan").magazineNo(2l).build();

		// 🧾 Purchase (FULL OBJECT – NO MOCK)
		MagazinePurchase purchase = MagazinePurchase.builder().id(100L).user(user) // 🔥 IMPORTANT
				.book(book) // 🔥 IMPORTANT
				.price(70).purchasedAt(LocalDateTime.now()).build();

		when(purchaseRepo.findByPurchasedAtBetweenOrderByPurchasedAtDesc(any(), any())).thenReturn(List.of(purchase));

		List<MagazinePurchaseAdminDto> result = service.getPurchasesBetweenDates(LocalDate.now().minusDays(5),
				LocalDate.now(), null);

		assertEquals(1, result.size());
		assertEquals("User One", result.get(0).getUserName());
		assertEquals("Magazine Jan", result.get(0).getBookTitle());
	}

	@Test
	void getPurchasesBetweenDates_withBook_success() {

		// 👤 User
		User user = User.builder().id(1L).name("User").email("user@test.com").mobile("9999999999").build();

		// 📘 Book
		Books book = Books.builder().id(2L).title("History Magazine").magazineNo(2l).build();

		// 🧾 Purchase (REAL ENTITY – NOT MOCK)
		MagazinePurchase purchase = MagazinePurchase.builder().id(100L).user(user).book(book).price(70)
				.purchasedAt(LocalDateTime.now()).build();

		when(bookRepo.findById(2L)).thenReturn(Optional.of(book));

		when(purchaseRepo.findByBookAndPurchasedAtBetweenOrderByPurchasedAtDesc(eq(book), any(), any()))
				.thenReturn(List.of(purchase));

		List<MagazinePurchaseAdminDto> result = service.getPurchasesBetweenDates(LocalDate.now().minusDays(10),
				LocalDate.now(), 2L);

		assertEquals(1, result.size());
		assertEquals("User", result.get(0).getUserName());
		assertEquals("History Magazine", result.get(0).getBookTitle());
	}

}
