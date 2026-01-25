package com.digital.magazine.subscription.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.subscription.entity.MagazinePurchase;
import com.digital.magazine.user.entity.User;

public interface MagazinePurchaseRepository extends JpaRepository<MagazinePurchase, Long> {

	boolean existsByUserAndBook(User user, Books book);

	// 🔹 All purchases
	List<MagazinePurchase> findAllByOrderByPurchasedAtDesc();

	// 🔹 By book
	List<MagazinePurchase> findByBookOrderByPurchasedAtDesc(Books book);

	// 🔹 Date range (ALL books)
	List<MagazinePurchase> findByPurchasedAtBetweenOrderByPurchasedAtDesc(LocalDateTime from, LocalDateTime to);

	// 🔹 Date range + specific book
	List<MagazinePurchase> findByBookAndPurchasedAtBetweenOrderByPurchasedAtDesc(Books book, LocalDateTime from,
			LocalDateTime to);

	@Query("""
				SELECT DISTINCT u.email
				FROM MagazinePurchase mp
				JOIN mp.user u
			""")
	List<String> findDistinctBuyerEmails();

}
