package com.digital.magazine.subscription.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SubscriptionPopupDto {

	boolean show;

	String status;

	String message;

	LocalDate endDate;

}
