package com.digital.magazine.admin.service;

import com.digital.magazine.admin.dto.DashboardStatsDto;
import com.digital.magazine.admin.dto.SubscriptionStatsResponse;

public interface AdminDashboardService {

	public DashboardStatsDto getDashboardStats();

	SubscriptionStatsResponse getStatsSummary(int expiringDays);

}
