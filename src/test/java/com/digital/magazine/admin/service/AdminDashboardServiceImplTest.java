package com.digital.magazine.admin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

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

		when(userRepo.count()).thenReturn(200L);
		when(userRepo.countByStatus(AccountStatus.PENDING)).thenReturn(15L);

		DashboardStatsDto stats = service.getDashboardStats();

		assertEquals(100L, stats.getTotalBooks());
		assertEquals(70L, stats.getPublishedBooks());
		assertEquals(30L, stats.getDraftBooks());
		assertEquals(200L, stats.getTotalUsers());
		assertEquals(15L, stats.getPendingUsers());
		assertEquals(12L, stats.getBooksUploadedThisMonth());

		verify(bookRepo).count();
		verify(bookRepo).countByStatus(BookStatus.PUBLISHED);
		verify(bookRepo).countByStatus(BookStatus.DRAFT);
		verify(bookRepo).countBooksUploadedThisMonth();
		verify(userRepo).count();
		verify(userRepo).countByStatus(AccountStatus.PENDING);
	}

	// --------------------------------------------------
	// ✅ getStatsSummary
	// --------------------------------------------------
	@Test
	void getStatsSummary_success() {

		int days = 7;

		when(userRepo.countByRole(Role.USER)).thenReturn(100L);
		when(subscriptionRepo.countPaidUsers(SubscriptionStatus.ACTIVE, Role.USER)).thenReturn(40L);
		when(subscriptionRepo.countExpiringSoon(eq(Role.USER), any(LocalDate.class), any(LocalDate.class)))
				.thenReturn(8L);

		SubscriptionStatsResponse response = service.getStatsSummary(days);

		assertEquals(60L, response.getFreeUsers()); // 100 - 40
		assertEquals(40L, response.getPaidUsers());
		assertEquals(8L, response.getExpiringSoon());

		verify(userRepo).countByRole(Role.USER);
		verify(subscriptionRepo).countPaidUsers(SubscriptionStatus.ACTIVE, Role.USER);
		verify(subscriptionRepo).countExpiringSoon(eq(Role.USER), any(LocalDate.class), any(LocalDate.class));
	}
}
