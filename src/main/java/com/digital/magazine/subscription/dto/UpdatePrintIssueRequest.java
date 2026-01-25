package com.digital.magazine.subscription.dto;

import java.time.LocalDate;

import com.digital.magazine.subscription.enums.DeliveryStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePrintIssueRequest {

	private Long magazineNo;
	private String courierName;
	private DeliveryStatus status; // ISSUED / DELIVERED
	private LocalDate deliveryDate;
}
