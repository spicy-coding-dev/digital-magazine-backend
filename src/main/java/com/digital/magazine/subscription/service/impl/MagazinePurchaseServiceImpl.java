package com.digital.magazine.subscription.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.common.exception.NoBooksFoundException;
import com.digital.magazine.common.exception.UserNotFoundException;
import com.digital.magazine.subscription.entity.MagazinePurchase;
import com.digital.magazine.subscription.repository.MagazinePurchaseRepository;
import com.digital.magazine.subscription.service.MagazinePurchaseService;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MagazinePurchaseServiceImpl implements MagazinePurchaseService {

	private final MagazinePurchaseRepository purchaseRepo;
	private final UserRepository userRepo;
	private final BookRepository bookRepo;

	@Override
	public void purchase(Authentication auth, Long bookId) {

		User user = userRepo.findByEmail(auth.getName()).orElseThrow(() -> new UserNotFoundException("User not found"));

		Books book = bookRepo.findById(bookId).orElseThrow(() -> new NoBooksFoundException("Book not found"));

		if (purchaseRepo.existsByUserAndBook(user, book)) {
			throw new IllegalStateException("இந்த இதழை நீங்கள் ஏற்கனவே வாங்கியுள்ளீர்கள்");
		}

		MagazinePurchase mp = MagazinePurchase.builder().user(user).book(book).price(70)
				.purchasedAt(LocalDateTime.now()).build();

		purchaseRepo.save(mp);

		log.info("Single magazine purchased successfully | user={} | book={}", user.getEmail(), book.getId());
	}

}
