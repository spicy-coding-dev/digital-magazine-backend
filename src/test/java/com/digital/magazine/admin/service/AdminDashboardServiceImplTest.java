package com.digital.magazine.admin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.digital.magazine.admin.dto.DashboardStatsDto;
import com.digital.magazine.admin.dto.SubscriptionStatsResponse;
import com.digital.magazine.admin.service.impl.AdminDashboardServiceImpl;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.common.enums.BookStatus;
import com.digital.magazine.common.enums.Role;
import com.digital.magazine.payment.repository.PaymentTransactionRepository;
import com.digital.magazine.subscription.enums.SubscriptionStatus;
import com.digital.magazine.subscription.repository.UserSubscriptionRepository;
import com.digital.magazine.user.enums.AccountStatus;
import com.digital.magazine.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceImplTest {

	@Mock
	private BookRepository bookRepo;

	@Mock
	private UserRepository userRepo;

	@Mock
	private UserSubscriptionRepository subscriptionRepo;

	@Mock
	private PaymentTransactionRepository paymentRepo;

	@InjectMocks
	private AdminDashboardServiceImpl service;

	// --------------------------------------------------
	// ✅ getDashboardStats
	// --------------------------------------------------
	@Test
	void getDashboardStats_success() {

		when(bookRepo.count()).thenReturn(100L);
		when(bookRepo.countByStatus(BookStatus.PUBLISHED)).thenReturn(70L);
		when(bookRepo.countByStatus(BookStatus.DRAFT)).thenReturn(30L);
		when(bookRepo.countBooksUploadedThisMonth()).thenReturn(12L);
		when(paymentRepo.getCurrentMonthTotalAmount()).thenReturn(5000.0);

		when(userRepo.countByRole(Role.USER)).thenReturn(200L);
		when(userRepo.countByStatus(AccountStatus.PENDING)).thenReturn(15L);

		DashboardStatsDto stats = service.getDashboardStats();

		assertEquals(100L, stats.getTotalBooks());
		assertEquals(70L, stats.getPublishedBooks());
		assertEquals(30L, stats.getDraftBooks());
		assertEquals(200L, stats.getTotalUsers());
		assertEquals(15L, stats.getPendingUsers());
		assertEquals(12L, stats.getBooksUploadedThisMonth());
		assertEquals(5000.0, stats.getCurrentMonthRevenue());

		verify(bookRepo).count();
		verify(bookRepo).countByStatus(BookStatus.PUBLISHED);
		verify(bookRepo).countByStatus(BookStatus.DRAFT);
		verify(bookRepo).countBooksUploadedThisMonth();
		verify(userRepo).countByRole(Role.USER);
		verify(userRepo).countByStatus(AccountStatus.PENDING);
		verify(paymentRepo).getCurrentMonthTotalAmount();
	}

	// --------------------------------------------------
	// ✅ getStatsSummary
	// --------------------------------------------------
	@Test
	void getStatsSummary_success() {

		when(userRepo.countByRole(Role.USER)).thenReturn(100L);
		when(subscriptionRepo.countPaidUsers(SubscriptionStatus.ACTIVE, Role.USER)).thenReturn(40L);
		when(subscriptionRepo.countPaidUsers(SubscriptionStatus.EXPIRING_SOON, Role.USER)).thenReturn(40L);

		SubscriptionStatsResponse response = service.getStatsSummary();

		assertEquals(60L, response.getFreeUsers()); // 100 - 40
		assertEquals(40L, response.getPaidUsers());
		assertEquals(40L, response.getExpiringSoon());

		verify(userRepo).countByRole(Role.USER);
		verify(subscriptionRepo).countPaidUsers(SubscriptionStatus.ACTIVE, Role.USER);
		verify(subscriptionRepo).countPaidUsers(SubscriptionStatus.EXPIRING_SOON, Role.USER);
	}
}
