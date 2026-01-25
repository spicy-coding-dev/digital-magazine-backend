package com.digital.magazine.payment.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.digital.magazine.payment.entity.PaymentTransaction;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

// 🔥 TODAY earnings
	@Query("""
			SELECT COALESCE(SUM(p.amount),0)
			FROM PaymentTransaction p
			WHERE p.status='SUCCESS'
			  AND DATE(p.paymentDate)=CURRENT_DATE
			""")
	double getTodayRevenue();

// 🔥 THIS MONTH earnings
	@Query("""
			SELECT COALESCE(SUM(p.amount),0)
			FROM PaymentTransaction p
			WHERE p.status='SUCCESS'
			  AND MONTH(p.paymentDate)=MONTH(CURRENT_DATE)
			  AND YEAR(p.paymentDate)=YEAR(CURRENT_DATE)
			""")
	double getThisMonthRevenue();

// 🔥 Custom range
	@Query("""
			SELECT COALESCE(SUM(p.amount),0)
			FROM PaymentTransaction p
			WHERE p.status='SUCCESS'
			  AND p.paymentDate BETWEEN :start AND :end
			""")
	double getRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
