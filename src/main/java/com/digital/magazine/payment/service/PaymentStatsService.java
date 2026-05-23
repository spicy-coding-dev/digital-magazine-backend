package com.digital.magazine.payment.service;

import java.util.List;

import com.digital.magazine.payment.dto.PaymentAdminResponseDto;
import com.digital.magazine.payment.dto.PaymentSummaryDto;

public interface PaymentStatsService {

	public PaymentSummaryDto getSummary();

	public List<PaymentAdminResponseDto> getAllPayments();

}
