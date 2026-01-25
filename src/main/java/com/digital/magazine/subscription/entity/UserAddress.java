package com.digital.magazine.subscription.entity;

import com.digital.magazine.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_addresses", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "address_line", "city",
		"state", "pincode" }))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAddress {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	private String name; // Home / Office
	private String addressLine;
	private String city;
	private String state;
	private String pincode;
	private String mobile;

	private boolean defaultAddress;
}
