package com.digital.magazine.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RazorpayOrderResponseDto {

	private String orderId;
	private Long amount; // paise
	private String currency;
	private String key;
}