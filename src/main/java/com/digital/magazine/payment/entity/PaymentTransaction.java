package com.digital.magazine.payment.entity;

import java.time.LocalDateTime;

import com.digital.magazine.payment.enums.PaymentStatus;
import com.digital.magazine.payment.enums.PaymentType;
import com.digital.magazine.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

	@ManyToOne
	private User user;

	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;

	private Double amount;

	private String currency;

	private String razorpayOrderId;

	private String razorpayPaymentId;

	private String gatewayRef;

	private Long bookId;

	private Long subscriptionPlanId;

	@Enumerated(EnumType.STRING)
	private PaymentStatus status;

	private LocalDateTime paymentDate;

}
