package com.digital.magazine.subscription.service.impl;

import java.time.LocalDate;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.digital.magazine.common.exception.AddressAccessDeniedException;
import com.digital.magazine.common.exception.AddressNotRequiredException;
import com.digital.magazine.common.exception.AddressRequiredException;
import com.digital.magazine.common.exception.DuplicateSubscriptionException;
import com.digital.magazine.common.exception.SubscriptionNotAllowedException;
import com.digital.magazine.common.exception.SubscriptionPlanNotFoundException;
import com.digital.magazine.common.exception.UserNotFoundException;
import com.digital.magazine.common.service.EmailService;
import com.digital.magazine.subscription.dto.BuySubscriptionRequest;
import com.digital.magazine.subscription.entity.PrintDelivery;
import com.digital.magazine.subscription.entity.SubscriptionPlan;
import com.digital.magazine.subscription.entity.UserAddress;
import com.digital.magazine.subscription.entity.UserSubscription;
import com.digital.magazine.subscription.enums.DeliveryStatus;
import com.digital.magazine.subscription.enums.SubscriptionStatus;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.subscription.repository.PrintDeliveryRepository;
import com.digital.magazine.subscription.repository.SubscriptionPlanRepository;
import com.digital.magazine.subscription.repository.UserAddressRepository;
import com.digital.magazine.subscription.repository.UserSubscriptionRepository;
import com.digital.magazine.subscription.service.SubscriptionService;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

	private final UserSubscriptionRepository userSubscriptionRepo;
	private final PrintDeliveryRepository printDeliveryRepo;
	private final UserAddressRepository addressRepo;
	private final SubscriptionPlanRepository subscriptionPlanRepo;
	private final UserRepository userRepo;
	private final EmailService emailService;

	@Override
	public String buy(BuySubscriptionRequest req, Authentication auth) {

		User user = userRepo.findByEmail(auth.getName())
				.orElseThrow(() -> new UserNotFoundException("பயனர் கிடைக்கவில்லை"));

		SubscriptionPlan plan = subscriptionPlanRepo.findById(req.getPlanId())
				.orElseThrow(() -> new SubscriptionPlanNotFoundException("சந்தா திட்டம் கிடைக்கவில்லை"));

		log.info("🛒 Subscription buy attempt | user={} | plan={}", user.getEmail(), plan.getName());

		// 🔴 RULE 0: DIGITAL_SINGLE not allowed here
		if (plan.getType() == SubscriptionType.DIGITAL && plan.getDurationYears() == 0) {
			log.warn("❌ DIGITAL_SINGLE attempted via subscription | user={}", user.getEmail());

			throw new SubscriptionNotAllowedException(
					"₹70 தனி இதழை சந்தாவாக வாங்க முடியாது. அந்த இதழின் 'Buy' பொத்தானை பயன்படுத்துங்கள்");
		}

		// 🔴 RULE 1: Any ACTIVE subscription already exists
		userSubscriptionRepo.findByUserAndStatus(user, SubscriptionStatus.ACTIVE).ifPresent(sub -> {
			log.warn("❌ Active subscription exists | user={} | existingPlan={}", user.getEmail(),
					sub.getPlan().getName());

			throw new DuplicateSubscriptionException("நீங்கள் ஏற்கனவே '" + sub.getPlan().getName()
					+ "' சந்தாவில் உள்ளீர்கள் (முடிவு தேதி: " + sub.getEndDate() + ")");
		});

		// 🔴 RULE 2: DIGITAL must NOT have address
		if (plan.getType() == SubscriptionType.DIGITAL && req.getAddressId() != null) {
			log.warn("❌ Address provided for DIGITAL subscription | user={}", user.getEmail());
			throw new AddressNotRequiredException("டிஜிட்டல் சந்தாவிற்கு முகவரி தேவையில்லை");
		}

		// 🔴 RULE 3: PRINT subscription requires address
		UserAddress address = null;
		if (plan.getType() == SubscriptionType.PRINT) {

			if (req.getAddressId() == null) {
				log.warn("❌ Address missing for PRINT subscription | user={}", user.getEmail());
				address = addressRepo.findByUserAndDefaultAddressTrue(user)
						.orElseThrow(() -> new AddressRequiredException("முகவரி அவசியம்"));
			}

			address = addressRepo.findByIdAndUser(req.getAddressId(), user)
					.orElseThrow(() -> new AddressAccessDeniedException("இந்த முகவரி உங்களுடையது அல்ல"));

		}

		// ✅ ACTIVATE SUBSCRIPTION
		UserSubscription sub = activateSubscription(user, plan, address);

		log.info("✅ Subscription activated | user={} | plan={}", user.getEmail(), plan.getName());

		if (sub.getPlan().getType() == SubscriptionType.PRINT) {
			emailService.sendPrintSubscriptionBuyMail(sub.getUser().getEmail(), sub.getPlan().getType(),
					sub.getUser().getName(), sub.getDeliveryAddress(), sub.getStartDate(), sub.getEndDate());
		} else {
			emailService.sendDigitalSubscriptionBuyMail(sub.getUser().getEmail(), sub.getPlan().getType(),
					sub.getUser().getName(), sub.getStartDate(), sub.getEndDate());
		}

		log.info("📧 Subscription confirmation mail sent | user={} | plan={}", sub.getUser().getEmail(),
				sub.getPlan().getName());

		// 🔥 SUCCESS MESSAGE (TAMIL)
		return "நீங்கள் '" + plan.getName() + "' சந்தாவை " + sub.getEndDate()
				+ " வரை வெற்றிகரமாக செயல்படுத்தியுள்ளீர்கள்";
	}

	private UserSubscription activateSubscription(User user, SubscriptionPlan plan, UserAddress address) {

		LocalDate start = LocalDate.now();
		LocalDate end = start.plusYears(plan.getDurationYears());

		UserSubscription sub = UserSubscription.builder().user(user).plan(plan).deliveryAddress(address) // 🔥 HERE
				.startDate(start).endDate(end).status(SubscriptionStatus.ACTIVE).build();

		userSubscriptionRepo.save(sub);

		log.info("Subscription activated | user={} | plan={}", user.getEmail(), plan.getName());

		if (plan.getType() == SubscriptionType.PRINT) {
			generatePrintDeliveries(sub);
		}

		return sub;
	}

	private void generatePrintDeliveries(UserSubscription sub) {

		int issues = sub.getPlan().getDurationYears() * 6;

		for (int i = 0; i < issues; i++) {

			PrintDelivery d = PrintDelivery.builder().subscription(sub)
					.deliveryDate(sub.getStartDate().plusMonths(i * 2)).status(DeliveryStatus.PENDING).build();

			printDeliveryRepo.save(d);
		}

		log.info("Print deliveries generated | subscription={}", sub.getId());
	}

}
