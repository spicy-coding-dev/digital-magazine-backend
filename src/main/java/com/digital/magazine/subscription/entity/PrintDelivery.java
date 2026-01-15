package com.digital.magazine.subscription.entity;

import java.time.LocalDate;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.subscription.enums.DeliveryStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "print_deliveries")
@Getter
@Setter
@Builder
public class PrintDelivery {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private UserSubscription subscription;

	@ManyToOne
	private Books book;

	private LocalDate deliveryDate;

	@Enumerated(EnumType.STRING)
	private DeliveryStatus status;
}
