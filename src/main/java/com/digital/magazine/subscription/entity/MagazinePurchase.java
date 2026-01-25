package com.digital.magazine.subscription.entity;

import java.time.LocalDateTime;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "magazine_purchases")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MagazinePurchase {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private User user;

	@ManyToOne
	private Books book;

	private double price;
	private LocalDateTime purchasedAt;
}
