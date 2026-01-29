package com.digital.magazine.subscription.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.common.enums.BookStatus;
import com.digital.magazine.common.exception.AlreadyPurchasedException;
import com.digital.magazine.common.exception.BookNotPurchasableException;
import com.digital.magazine.common.exception.DigitalSubscriptionExistsException;
import com.digital.magazine.common.exception.FreeBookException;
import com.digital.magazine.common.exception.NoBooksFoundException;
import com.digital.magazine.common.exception.UserNotFoundException;
import com.digital.magazine.subscription.entity.MagazinePurchase;
import com.digital.magazine.subscription.entity.UserSubscription;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.subscription.repository.MagazinePurchaseRepository;
import com.digital.magazine.subscription.repository.UserSubscriptionRepository;
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
	private final UserSubscriptionRepository userSubscriptionRepo;

	@Override
	public String purchase(Authentication auth, Long bookId) {

		User user = userRepo.findByEmail(auth.getName())
				.orElseThrow(() -> new UserNotFoundException("பயனர் காணப்படவில்லை"));

		Books book = bookRepo.findById(bookId).orElseThrow(() -> new NoBooksFoundException("இதழ் கிடைக்கவில்லை"));

		log.info("Single book purchase attempt | user={} | book={}", user.getEmail(), book.getId());

		// 🔴 RULE 1: Book is FREE
		if (!book.isPaid()) {
			log.warn("Purchase blocked - book is free | bookId={}", bookId);
			throw new FreeBookException("இந்த புத்தகம் இலவசமாக கிடைக்கிறது");
		}

		if (book.getStatus() == BookStatus.DRAFT || book.getStatus() == BookStatus.BLOCKED) {

			log.warn("Purchase blocked - book is free | bookId={}", bookId);
			throw new BookNotPurchasableException("இந்த புத்தகம் தற்போது வாங்க முடியாது");

		}

		// 🔴 RULE 2: User has active DIGITAL subscription
		UserSubscription activeSub = userSubscriptionRepo.findActiveByUser(user).orElse(null);

		if (activeSub != null && activeSub.getPlan().getType() == SubscriptionType.DIGITAL) {

			log.warn("Purchase blocked - user has digital subscription | user={}", user.getEmail());
			throw new DigitalSubscriptionExistsException("உங்களுக்கு ஏற்கனவே டிஜிட்டல் சந்தா உள்ளது");
		}

		// 🔴 RULE 3: Already purchased
		if (purchaseRepo.existsByUserAndBook(user, book)) {
			log.warn("Purchase blocked - already purchased | user={} | book={}", user.getEmail(), book.getId());
			throw new AlreadyPurchasedException("இந்த இதழை நீங்கள் ஏற்கனவே வாங்கியுள்ளீர்கள்");
		}

		// ✅ FINAL PURCHASE
		MagazinePurchase mp = MagazinePurchase.builder().user(user).book(book).price(book.getPrice()) // 🔥 dynamic
																										// price
				.purchasedAt(LocalDateTime.now()).build();

		purchaseRepo.save(mp);

		log.info("Single magazine purchased successfully | user={} | book={}", user.getEmail(), book.getId());

		// 🔥 SUCCESS MESSAGE
		return "நீங்கள் வாங்கிய இதழ் : " + book.getTitle() + " (இதழ் எண் : " + book.getMagazineNo()
				+ ") வெற்றிகரமாக திறக்கப்பட்டது";
	}

}
