package com.digital.magazine.admin.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.digital.magazine.admin.dto.DashboardStatsDto;
import com.digital.magazine.admin.dto.SubscriptionStatsResponse;
import com.digital.magazine.admin.service.AdminDashboardService;
import com.digital.magazine.payment.dto.PaymentSummaryDto;
import com.digital.magazine.payment.service.PaymentStatsService;
import com.digital.magazine.security.jwt.JwtAuthenticationFilter;
import com.digital.magazine.security.jwt.JwtUtil;

/**
 * Controller layer test ONLY
 */
@WebMvcTest(AdminDashboardController.class)
@AutoConfigureMockMvc
class AdminDashboardControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AdminDashboardService dashboardService;

	@MockBean
	private PaymentStatsService paymentStatsService;

	@MockBean
	private JwtUtil jwtUtil;

	@MockBean
	private JwtAuthenticationFilter jwtFilter;

	@MockBean
	private UserDetailsService userDetailsService;

	// ----------------------------------------
	// ✅ /stats
	// ----------------------------------------
	@WithMockUser(username = "admin@test.com", roles = "ADMIN")
	@Test
	void getDashboardStats_success() throws Exception {

		DashboardStatsDto dto = DashboardStatsDto.builder().totalBooks(10).publishedBooks(6).draftBooks(4)
				.totalUsers(50).pendingUsers(5).booksUploadedThisMonth(3).build();

		when(dashboardService.getDashboardStats()).thenReturn(dto);

		mockMvc.perform(get("/api/v1/admin/dashboard/stats")).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalBooks").value(10)).andExpect(jsonPath("$.data.totalUsers").value(50));
	}

	// ----------------------------------------
	// ✅ /subs/summary
	// ----------------------------------------
	@Test
	@WithMockUser(username = "admin@test.com", roles = "ADMIN")
	void getSubscriptionSummary_success() throws Exception {

		SubscriptionStatsResponse response = SubscriptionStatsResponse.builder().freeUsers(60).paidUsers(40)
				.expiringSoon(5).build();

		when(dashboardService.getStatsSummary(7)).thenReturn(response);

		mockMvc.perform(get("/api/v1/admin/dashboard/subs/summary").param("days", "7")).andExpect(status().isOk())
				.andExpect(jsonPath("$.freeUsers").value(60)).andExpect(jsonPath("$.paidUsers").value(40))
				.andExpect(jsonPath("$.expiringSoon").value(5));
	}

	// ----------------------------------------
	// ✅ /payment/summary
	// ----------------------------------------
	@Test
	@WithMockUser(username = "admin@test.com", roles = "ADMIN")
	void getPaymentSummary_success() throws Exception {

		PaymentSummaryDto dto = PaymentSummaryDto.builder().todayRevenue(100).thisMonthRevenue(500).build();

		when(paymentStatsService.getSummary()).thenReturn(dto);

		mockMvc.perform(get("/api/v1/admin/dashboard/payment/summary")).andExpect(status().isOk())
				.andExpect(jsonPath("$.todayRevenue").value(100)).andExpect(jsonPath("$.thisMonthRevenue").value(500));
	}
}
