package com.digital.magazine.subscription.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.digital.magazine.subscription.entity.UserAddress;
import com.digital.magazine.user.entity.User;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

	List<UserAddress> findByUser(User user);

	Optional<UserAddress> findByIdAndUser(Long id, User user);

	Optional<UserAddress> findByUserAndDefaultAddressTrue(User user);

	boolean existsByUserAndAddressLineAndCityAndStateAndPincode(User user, String addressLine, String city,
			String state, String pincode);

}
