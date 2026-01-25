package com.digital.magazine.subscription.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MagazinePurchaseAdminDto {

	private Long purchaseId;

	private Long userId;
	private String userName;
	private String userEmail;
	private String mobile;

	private String bookTitle;
	private Long magazineNo;

	private double price;
	private LocalDateTime purchasedAt;

}
