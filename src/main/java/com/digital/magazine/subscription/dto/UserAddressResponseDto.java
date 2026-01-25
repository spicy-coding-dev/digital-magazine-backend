package com.digital.magazine.subscription.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAddressResponseDto {

	private Long id;
	private String name;
	private String addressLine;
	private String city;
	private String state;
	private String pincode;
	private String mobile;
	private boolean defaultAddress;
}
