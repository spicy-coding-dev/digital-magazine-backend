package com.digital.magazine.subscription.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.subscription.entity.PrintDelivery;
import com.digital.magazine.subscription.enums.DeliveryStatus;

public interface PrintDeliveryRepository extends JpaRepository<PrintDelivery, Long> {

	List<PrintDelivery> findBySubscriptionId(Long id);

	Optional<PrintDelivery> findFirstByBookAndStatusOrderByDeliveryDateAsc(Books book, DeliveryStatus status);

	List<PrintDelivery> findByStatusAndDeliveryDate(DeliveryStatus status, LocalDate deliveryDate);

}
