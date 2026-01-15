package com.digital.magazine.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.subscription.entity.MagazinePurchase;
import com.digital.magazine.user.entity.User;

public interface MagazinePurchaseRepository extends JpaRepository<MagazinePurchase, Long> {

	boolean existsByUserAndBook(User user, Books book);

}
