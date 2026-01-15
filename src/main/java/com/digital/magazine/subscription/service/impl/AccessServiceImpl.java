package com.digital.magazine.subscription.service.impl;

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

//	@Override
//	public boolean canAccessBook(User user, Books book) {
//
//		boolean hasDigital = userSubscriptionRepo.existsByUserAndPlan_TypeAndStatus(user, SubscriptionType.DIGITAL,
//				SubscriptionStatus.ACTIVE);
//
//		boolean hasSingle = magazinePurchaseRepo.existsByUserAndBook(user, book);
//
//		log.info("Access check | user={} | book={} | digital={} | single={}", user.getEmail(), book.getId(), hasDigital,
//				hasSingle);
//
//		return hasDigital || hasSingle;
//	}
}
