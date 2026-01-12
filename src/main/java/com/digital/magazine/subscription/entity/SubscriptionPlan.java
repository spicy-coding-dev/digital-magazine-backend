package com.digital.magazine.subscription.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.digital.magazine.subscription.enums.SubscriptionType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscription_plans", uniqueConstraints = { @UniqueConstraint(columnNames = "plan_code") })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Business identifier (frontend uses this)
	@Column(name = "plan_code", nullable = false, unique = true)
	private String planCode;

	// Tamil display name
	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SubscriptionType type; // PRINT / DIGITAL

	// 0 = single issue / donation
	@Column(name = "duration_years", nullable = false)
	private int durationYears;

	@Column(nullable = false)
	private BigDecimal price;

	@Column(nullable = false)
	private boolean active = true;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@PrePersist
	void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
