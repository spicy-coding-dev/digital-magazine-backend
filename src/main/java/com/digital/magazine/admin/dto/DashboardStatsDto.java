package com.digital.magazine.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardStatsDto {

	private long totalBooks;
	private long publishedBooks;
	private long draftBooks;

	private long totalUsers;
	private long pendingUsers;

	private long booksUploadedThisMonth;
}

