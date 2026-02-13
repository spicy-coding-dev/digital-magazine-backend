package com.digital.magazine.payment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentSummaryDto {

	private double todayRevenue;
	private double thisMonthRevenue;
}
