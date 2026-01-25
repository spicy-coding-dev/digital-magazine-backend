package com.digital.magazine.subscription.dto;

import java.time.LocalDate;

import com.digital.magazine.subscription.enums.DeliveryStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrintDeliveryResponseDto {

	private Long deliveryId;

	private Long subscriptionId;

	private Long userId;
	private String userName;
	private String userEmail;

	private String addressName;
	private String addressLine;
	private String city;
	private String state;
	private String pincode;
	private String mobile;

	private String bookTitle;
	private Long magazineNo;

	private LocalDate deliveryDate;

	private String courierName;
	private String issuedBy;

	private DeliveryStatus status;
}
