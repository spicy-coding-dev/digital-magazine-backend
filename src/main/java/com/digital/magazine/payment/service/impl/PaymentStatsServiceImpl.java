package com.digital.magazine.payment.service.impl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.digital.magazine.payment.dto.PaymentSummaryDto;
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

		log.info("💰 Revenue | today={} | month={}", today, month);

		return PaymentSummaryDto.builder().todayRevenue(today).thisMonthRevenue(month).build();
	}

	public double getCustomRange(LocalDate from, LocalDate to) {

		log.info("📅 Revenue range | {} → {}", from, to);

		return repo.getRevenueBetween(from.atStartOfDay(), to.atTime(23, 59, 59));
	}

}
