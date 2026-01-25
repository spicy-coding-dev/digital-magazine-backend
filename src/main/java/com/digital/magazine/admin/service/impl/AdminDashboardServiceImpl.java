package com.digital.magazine.admin.service.impl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.digital.magazine.admin.dto.DashboardStatsDto;
import com.digital.magazine.admin.dto.SubscriptionStatsResponse;
import com.digital.magazine.admin.service.AdminDashboardService;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.common.enums.BookStatus;
import com.digital.magazine.common.enums.Role;
import com.digital.magazine.subscription.enums.SubscriptionStatus;
import com.digital.magazine.subscription.repository.UserSubscriptionRepository;
import com.digital.magazine.user.enums.AccountStatus;
import com.digital.magazine.user.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {

	private final BookRepository bookRepo;
	private final UserRepository userRepo;
	private final UserSubscriptionRepository subscriptionRepo;

	@Override
	public DashboardStatsDto getDashboardStats() {

		log.info("📊 Fetching admin dashboard statistics");

		long totalBooks = bookRepo.count();
		long publishedBooks = bookRepo.countByStatus(BookStatus.PUBLISHED);
		long draftBooks = bookRepo.countByStatus(BookStatus.DRAFT);

		long totalUsers = userRepo.count();
		long pendingUsers = userRepo.countByStatus(AccountStatus.PENDING);

		long booksThisMonth = bookRepo.countBooksUploadedThisMonth();

		return DashboardStatsDto.builder().totalBooks(totalBooks).publishedBooks(publishedBooks).draftBooks(draftBooks)
				.totalUsers(totalUsers).pendingUsers(pendingUsers).booksUploadedThisMonth(booksThisMonth).build();
	}

	@Override
	public SubscriptionStatsResponse getStatsSummary(int days) {

		log.info("📊 Fetching USER subscription stats only (excluding admin)");

		// ✅ ONLY ROLE_USER
		long totalUsers = userRepo.countByRole(Role.USER);

		long paidUsers = subscriptionRepo.countPaidUsers(SubscriptionStatus.ACTIVE, Role.USER);

		long freeUsers = totalUsers - paidUsers;

		long expiringSoon = subscriptionRepo.countExpiringSoon(Role.USER, LocalDate.now(),
				LocalDate.now().plusDays(days));

		log.info("📈 USER Stats → free={}, paid={}, expiringSoon={}", freeUsers, paidUsers, expiringSoon);

		return SubscriptionStatsResponse.builder().freeUsers(freeUsers).paidUsers(paidUsers).expiringSoon(expiringSoon)
				.build();
	}

}
