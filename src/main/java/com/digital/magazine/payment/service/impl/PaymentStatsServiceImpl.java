package com.digital.magazine.payment.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.digital.magazine.payment.dto.PaymentAdminResponseDto;
import com.digital.magazine.payment.dto.PaymentSummaryDto;
import com.digital.magazine.payment.entity.PaymentTransaction;
import com.digital.magazine.payment.repository.PaymentTransactionRepository;
import com.digital.magazine.payment.service.PaymentStatsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentStatsServiceImpl implements PaymentStatsService {

	private final PaymentTransactionRepository repo;

	@Override
	public PaymentSummaryDto getSummary() {

		log.info("📊 Fetching payment summary");

		double today = repo.getTodayRevenue();
		double month = repo.getThisMonthRevenue();
		double lastMonth = repo.getLastMonthRevenue();

		log.info("💰 Revenue | today={} | month={} | lastMonth={}", today, month, lastMonth);

		return PaymentSummaryDto.builder().todayRevenue(today).thisMonthRevenue(month).thisLastRevenue(lastMonth)
				.build();
	}

	public double getCustomRange(LocalDate from, LocalDate to) {

		log.info("📅 Revenue range | {} → {}", from, to);

		return repo.getRevenueBetween(from.atStartOfDay(), to.atTime(23, 59, 59));
	}

	@Override
	public List<PaymentAdminResponseDto> getAllPayments() {

		log.info("💰 Fetching all payment transactions");

		List<PaymentTransaction> payments = repo.findAllByOrderByPaymentDateDesc();

		log.info("✅ Total payment transactions fetched = {}", payments.size());

		return payments.stream().map(this::mapToDto).toList();
	}

	/* 🔥 MAP ENTITY → DTO */
	private PaymentAdminResponseDto mapToDto(PaymentTransaction p) {

		return PaymentAdminResponseDto.builder()

				.id(p.getId())

				.userName(p.getUser() != null ? p.getUser().getName() : null)

				.email(p.getUser() != null ? p.getUser().getEmail() : null)

				.paymentType(p.getPaymentType())

				.amount(p.getAmount())

				.currency(p.getCurrency())

				.razorpayPaymentId(p.getRazorpayPaymentId())

				.razorpayOrderId(p.getRazorpayOrderId())

				.status(p.getStatus())

				.paymentDate(p.getPaymentDate())

				.bookId(p.getBookId())

				.subscriptionPlanId(p.getSubscriptionPlanId())

				.build();
	}

}
