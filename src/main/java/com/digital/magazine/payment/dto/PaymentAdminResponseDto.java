package com.digital.magazine.payment.dto;

import java.time.LocalDateTime;

import com.digital.magazine.payment.enums.PaymentStatus;
import com.digital.magazine.payment.enums.PaymentType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentAdminResponseDto {

	private Long id;

	private String userName;

	private String email;

	private PaymentType paymentType;

	private Double amount;

	private String currency;

	private String razorpayPaymentId;

	private String razorpayOrderId;

	private PaymentStatus status;

	private LocalDateTime paymentDate;

	private Long bookId;

	private Long subscriptionPlanId;
}