package com.digital.magazine.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.digital.magazine.subscription.entity.UserAddress;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

}
