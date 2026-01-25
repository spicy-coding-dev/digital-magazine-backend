package com.digital.magazine.subscription.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.digital.magazine.subscription.dto.SaveAddressRequestDto;
import com.digital.magazine.subscription.dto.UserAddressResponseDto;

public interface UserAddressService {

	UserAddressResponseDto saveAddress(SaveAddressRequestDto dto, Authentication auth);

	List<UserAddressResponseDto> getMyAddresses(Authentication auth);

}
