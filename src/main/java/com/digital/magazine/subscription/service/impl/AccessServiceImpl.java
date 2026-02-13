package com.digital.magazine.subscription.service.impl;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.subscription.enums.SubscriptionStatus;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.subscription.repository.MagazinePurchaseRepository;
import com.digital.magazine.subscription.repository.UserSubscriptionRepository;
import com.digital.magazine.subscription.service.AccessService;
import com.digital.magazine.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccessServiceImpl implements AccessService {

	private final UserSubscriptionRepository userSubscriptionRepo;
	private final MagazinePurchaseRepository magazinePurchaseRepo;

	@Override
	public boolean canAccessBook(User user, Books book) {

		log.info("🔐 Access check | userId={}, bookId={}", user != null ? user.getId() : "GUEST", book.getId());

		// 🔓 Free book – always open
		if (!book.isPaid()) {
			return true;
		}

		// 🚫 Paid book + guest user
		if (user == null) {
			return false;
		}

		Long userId = user.getId();

		// 🔥 Digital subscription – ALL books open
		if (hasDigitalSubscription(userId)) {
			return true;
		}

		Set<Long> purchasedBooks = magazinePurchaseRepo.findBookIdsByUserId(userId);

		log.info("🛒 Purchased book IDs for user {} = {}", userId, purchasedBooks);

		if (purchasedBooks.contains(book.getId())) {
			log.info("✅ Individual purchase MATCHED");
			return true;
		}

		// 📰 Print subscription – web-la benefit illa
		return false;
	}

	private boolean hasDigitalSubscription(Long userId) {
		return userSubscriptionRepo.existsByUser_IdAndPlan_TypeAndStatus(userId, SubscriptionType.DIGITAL,
				SubscriptionStatus.ACTIVE);
	}
}
