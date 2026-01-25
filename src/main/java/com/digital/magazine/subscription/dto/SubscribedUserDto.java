package com.digital.magazine.subscription.dto;

import java.time.LocalDate;

import com.digital.magazine.subscription.enums.SubscriptionStatus;
import com.digital.magazine.subscription.enums.SubscriptionType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SubscribedUserDto {

	private Long userId;
	private String name;
	private String email;

	private String planName;
	private SubscriptionType planType;

	private LocalDate startDate;
	private LocalDate endDate;

	private UserAddressResponseDto address;

	private Long subscriptionId;

	private SubscriptionStatus status;
}
