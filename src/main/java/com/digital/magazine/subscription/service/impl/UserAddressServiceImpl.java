package com.digital.magazine.subscription.service.impl;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.digital.magazine.common.exception.DuplicateAddressException;
import com.digital.magazine.common.exception.UserNotFoundException;
import com.digital.magazine.subscription.dto.SaveAddressRequestDto;
import com.digital.magazine.subscription.dto.UserAddressResponseDto;
import com.digital.magazine.subscription.entity.UserAddress;
import com.digital.magazine.subscription.repository.UserAddressRepository;
import com.digital.magazine.subscription.service.UserAddressService;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

	private final UserRepository userRepo;
	private final UserAddressRepository addressRepo;

	@Override
	public UserAddressResponseDto saveAddress(SaveAddressRequestDto dto, Authentication auth) {

		log.info("📦 Saving address | user={}", auth.getName());

		User user = userRepo.findByEmail(auth.getName())
				.orElseThrow(() -> new UserNotFoundException("பயனர் காணப்படவில்லை"));

		// 🔥 DUPLICATE CHECK (same user only)
		boolean exists = addressRepo.existsByUserAndAddressLineAndCityAndStateAndPincode(user, dto.getAddressLine(),
				dto.getCity(), dto.getState(), dto.getPincode());

		if (exists) {
			throw new DuplicateAddressException("இந்த முகவரி ஏற்கனவே சேர்க்கப்பட்டுள்ளது");
		}

		// 🔥 Reset old default
		if (dto.isDefaultAddress()) {
			List<UserAddress> addresses = addressRepo.findByUser(user);
			addresses.forEach(a -> a.setDefaultAddress(false));
			addressRepo.saveAll(addresses);
		}

		UserAddress address = UserAddress.builder().user(user).name(dto.getName()).addressLine(dto.getAddressLine())
				.city(dto.getCity()).state(dto.getState()).pincode(dto.getPincode()).mobile(dto.getMobile())
				.defaultAddress(dto.isDefaultAddress()).build();

		addressRepo.save(address);

		log.info("✅ Address saved | addressId={}", address.getId());
		return mapToDto(address);
	}

	@Override
	public List<UserAddressResponseDto> getMyAddresses(Authentication auth) {

		log.info("📍 Fetching addresses | user={}", auth.getName());

		User user = userRepo.findByEmail(auth.getName())
				.orElseThrow(() -> new UserNotFoundException("பயனர் காணப்படவில்லை"));

		return addressRepo.findByUser(user).stream().map(this::mapToDto).toList();
	}

	private UserAddressResponseDto mapToDto(UserAddress a) {

		return UserAddressResponseDto.builder().id(a.getId()).name(a.getName()).addressLine(a.getAddressLine())
				.city(a.getCity()).state(a.getState()).pincode(a.getPincode()).mobile(a.getMobile())
				.defaultAddress(a.isDefaultAddress()).build();
	}
}
