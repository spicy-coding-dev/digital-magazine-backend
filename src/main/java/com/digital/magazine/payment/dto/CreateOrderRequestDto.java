package com.digital.magazine.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequestDto {
	private Double amount;
	private String purpose;
}
