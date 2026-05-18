package com.digital.magazine.payment.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.common.enums.BookStatus;
import com.digital.magazine.common.exception.AddressAccessDeniedException;
import com.digital.magazine.common.exception.AddressNotRequiredException;
import com.digital.magazine.common.exception.AddressRequiredException;
import com.digital.magazine.common.exception.AlreadyPurchasedException;
import com.digital.magazine.common.exception.BookNotPurchasableException;
import com.digital.magazine.common.exception.DigitalSubscriptionExistsException;
import com.digital.magazine.common.exception.DuplicateSubscriptionException;
import com.digital.magazine.common.exception.FreeBookException;
import com.digital.magazine.common.exception.NoBooksFoundException;
import com.digital.magazine.common.exception.SubscriptionNotAllowedException;
import com.digital.magazine.common.exception.SubscriptionPlanNotFoundException;
import com.digital.magazine.common.exception.UserNotFoundException;
import com.digital.magazine.payment.service.PaymentPreCheckService;
import com.digital.magazine.subscription.dto.BuySubscriptionRequest;
import com.digital.magazine.subscription.entity.SubscriptionPlan;
import com.digital.magazine.subscription.entity.UserSubscription;
import com.digital.magazine.subscription.enums.SubscriptionStatus;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.subscription.repository.MagazinePurchaseRepository;
import com.digital.magazine.subscription.repository.SubscriptionPlanRepository;
import com.digital.magazine.subscription.repository.UserAddressRepository;
import com.digital.magazine.subscription.repository.UserSubscriptionRepository;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentPreCheckServiceImpl implements PaymentPreCheckService {

	private final UserRepository userRepo;
	private final BookRepository bookRepo;
	private final MagazinePurchaseRepository purchaseRepo;
	private final UserSubscriptionRepository subscriptionRepo;
	private final SubscriptionPlanRepository planRepo;
	private final UserAddressRepository addressRepo;

	// 🔹 SINGLE BOOK
	@Override
	public void preCheckSingleBook(Authentication auth, Long bookId) {

		User user = userRepo.findByEmail(auth.getName())
				.orElseThrow(() -> new UserNotFoundException("பயனர் கிடைக்கவில்லை"));

		Books book = bookRepo.findById(bookId).orElseThrow(() -> new NoBooksFoundException("இதழ் கிடைக்கவில்லை"));

		log.info("🔍 Checking book rules | bookId={}", bookId);

		if (!book.isPaid())
			throw new FreeBookException("இந்த புத்தகம் இலவசமாக கிடைக்கிறது");

		if (book.getStatus() != BookStatus.PUBLISHED)
			throw new BookNotPurchasableException("இந்த புத்தகம் தற்போது வாங்க முடியாது");

		if (purchaseRepo.existsByUserAndBook(user, book))
			throw new AlreadyPurchasedException("இந்த இதழை நீங்கள் ஏற்கனவே வாங்கியுள்ளீர்கள்");

		UserSubscription sub = subscriptionRepo.findActiveByUser(user).orElse(null);

		if (sub != null && sub.getPlan().getType() == SubscriptionType.DIGITAL)
			throw new DigitalSubscriptionExistsException("உங்களுக்கு ஏற்கனவே டிஜிட்டல் சந்தா உள்ளது");

		log.info("✅ Pre-check single book PASSED");
	}

	// 🔹 SUBSCRIPTION
	@Override
	public void preCheckSubscription(Authentication auth, BuySubscriptionRequest req) {

		User user = userRepo.findByEmail(auth.getName())
				.orElseThrow(() -> new UserNotFoundException("பயனர் கிடைக்கவில்லை"));

		SubscriptionPlan plan = planRepo.findById(req.getPlanId())
				.orElseThrow(() -> new SubscriptionPlanNotFoundException("சந்தா திட்டம் கிடைக்கவில்லை"));

		log.info("🔍 Checking subscription rules | plan={}", plan.getName());

		// 🔴 RULE 0: DIGITAL_SINGLE not allowed here
		if (plan.getType() == SubscriptionType.DIGITAL && plan.getDurationYears() == 0) {
			log.warn("❌ DIGITAL_SINGLE attempted via subscription | user={}", user.getEmail());

			throw new SubscriptionNotAllowedException(
					"₹70 தனி இதழை சந்தாவாக வாங்க முடியாது. அந்த இதழின் 'Buy' பொத்தானை பயன்படுத்துங்கள்");
		}

		// 🔴 RULE 1: Any ACTIVE subscription already exists
		subscriptionRepo.findByUserAndStatus(user, SubscriptionStatus.ACTIVE).ifPresent(sub -> {
			log.warn("❌ Active subscription exists | user={} | existingPlan={}", user.getEmail(),
					sub.getPlan().getName());

			throw new DuplicateSubscriptionException("நீங்கள் ஏற்கனவே '" + sub.getPlan().getName()
					+ "' சந்தாவில் உள்ளீர்கள் (முடிவு தேதி: " + sub.getEndDate() + ")");
		});

		if (plan.getType() == SubscriptionType.DIGITAL && req.getAddressId() != null) {
			log.warn("❌ Address provided for DIGITAL subscription | user={}", user.getEmail());
			throw new AddressNotRequiredException("டிஜிட்டல் சந்தாவிற்கு முகவரி தேவையில்லை");
		}

		if (plan.getType() == SubscriptionType.PRINT) {

			if (req.getAddressId() == null) {
				log.warn("❌ Address missing for PRINT subscription | user={}", user.getEmail());
				addressRepo.findByUserAndDefaultAddressTrue(user)
						.orElseThrow(() -> new AddressRequiredException("முகவரி அவசியம்"));
			}

			addressRepo.findByIdAndUser(req.getAddressId(), user)
					.orElseThrow(() -> new AddressAccessDeniedException("இந்த முகவரி உங்களுடையது அல்ல"));

		}

		log.info("✅ Pre-check subscription PASSED");
	}
}
