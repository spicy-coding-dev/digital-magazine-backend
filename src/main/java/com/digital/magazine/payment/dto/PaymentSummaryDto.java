package com.digital.magazine.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentSummaryDto {

	private double todayRevenue;
	private double thisMonthRevenue;
}
