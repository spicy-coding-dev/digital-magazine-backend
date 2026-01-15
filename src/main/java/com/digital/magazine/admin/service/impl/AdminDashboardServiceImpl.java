package com.digital.magazine.admin.service.impl;

import org.springframework.stereotype.Service;

import com.digital.magazine.admin.dto.DashboardStatsDto;
import com.digital.magazine.admin.service.AdminDashboardService;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.common.enums.BookStatus;
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

	@Override
	public DashboardStatsDto getDashboardStats() {

		log.info("📊 Fetching admin dashboard statistics");

		long totalBooks = bookRepo.count();
		long publishedBooks = bookRepo.countByStatus(BookStatus.PUBLISHED);
		long draftBooks = bookRepo.countByStatus(BookStatus.DRAFT);

		long totalUsers = userRepo.count();
		long pendingUsers = userRepo.countByStatus(AccountStatus.PENDING);

		long booksThisMonth = bookRepo.countBooksUploadedThisMonth();

		return DashboardStatsDto.builder()
			.totalBooks(totalBooks)
			.publishedBooks(publishedBooks)
			.draftBooks(draftBooks)
			.totalUsers(totalUsers)
			.pendingUsers(pendingUsers)
			.booksUploadedThisMonth(booksThisMonth)
			.build();
	}
}

