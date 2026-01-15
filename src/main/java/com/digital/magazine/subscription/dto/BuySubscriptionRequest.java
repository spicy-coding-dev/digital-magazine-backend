package com.digital.magazine.subscription.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuySubscriptionRequest {

	private Long planId;
	private Long addressId; // only for PRINT
}
