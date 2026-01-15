package com.digital.magazine.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.digital.magazine.subscription.entity.PrintDelivery;

public interface PrintDeliveryRepository extends JpaRepository<PrintDelivery, Long> {

}
