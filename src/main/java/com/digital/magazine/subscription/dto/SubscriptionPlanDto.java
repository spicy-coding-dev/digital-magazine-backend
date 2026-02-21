package com.digital.magazine.subscription.dto;

import java.math.BigDecimal;

import com.digital.magazine.subscription.enums.SubscriptionType;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionPlanDto {

	private Long planId;
	private String planCode;
	private String name;
	private SubscriptionType type;
	private int durationYears;
	private BigDecimal price;
}
