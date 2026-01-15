package com.digital.magazine.subscription.entity;

import com.digital.magazine.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_addresses")
@Getter
@Setter
public class UserAddress {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private User user;

	private String name;
	private String mobile;
	private String address;
	private String city;
	private String state;
	private String pincode;
}
