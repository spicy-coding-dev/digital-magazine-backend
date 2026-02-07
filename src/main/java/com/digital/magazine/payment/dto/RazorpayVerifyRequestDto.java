package com.digital.magazine.payment.dto;

import com.digital.magazine.payment.enums.PaymentType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RazorpayVerifyRequestDto {

	// Razorpay mandatory
	private String razorpayOrderId;
	private String razorpayPaymentId;
	private String razorpaySignature;

	// Business data (frontend sends)
	private PaymentType paymentType; // SUBSCRIPTION / SINGLE_BOOK
	private Double amount;

	// Subscription related
	private Long planId;
	private Long addressId;

	// Single book
	private Long bookId;
}
