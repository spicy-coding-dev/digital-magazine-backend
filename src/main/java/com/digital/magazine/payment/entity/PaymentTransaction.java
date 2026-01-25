package com.digital.magazine.payment.entity;

import java.time.LocalDateTime;

import com.digital.magazine.payment.enums.PaymentStatus;
import com.digital.magazine.payment.enums.PaymentType;
import com.digital.magazine.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// who paid
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	// SUBSCRIPTION / SINGLE_BOOK / DONATION
	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;

	private Double amount;

	private LocalDateTime paymentDate;

	// SUCCESS / FAILED
	@Enumerated(EnumType.STRING)
	private PaymentStatus status;

	// Razorpay order / payment id
	private String gatewayRef;
}
