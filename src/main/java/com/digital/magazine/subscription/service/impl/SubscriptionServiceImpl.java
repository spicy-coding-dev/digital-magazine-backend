package com.digital.magazine.subscription.service.impl;

import java.time.LocalDate;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

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

	@Override
	public void buy(BuySubscriptionRequest req, Authentication auth) {

		User user = userRepo.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));

		SubscriptionPlan plan = subscriptionPlanRepo.findById(req.getPlanId())
				.orElseThrow(() -> new RuntimeException("Plan not found"));

		log.info("Subscription buy | user={} | plan={}", user.getEmail(), plan.getName());

		UserAddress address = null;

		if (plan.getType() == SubscriptionType.PRINT) {
			if (req.getAddressId() == null) {
				throw new IllegalStateException("Print subscription requires address");
			}

			address = addressRepo.findById(req.getAddressId())
					.orElseThrow(() -> new RuntimeException("Address not found"));
		}

		activateSubscription(user, plan, address);
	}

	@Override
	public void activateSubscription(User user, SubscriptionPlan plan, UserAddress address) {

		LocalDate start = LocalDate.now();
		LocalDate end = start.plusYears(plan.getDurationYears());

		UserSubscription sub = UserSubscription.builder().user(user).plan(plan).startDate(start).endDate(end)
				.status(SubscriptionStatus.ACTIVE).build();

		userSubscriptionRepo.save(sub);

		log.info("Subscription activated | user={} | plan={}", user.getEmail(), plan.getName());

		if (plan.getType() == SubscriptionType.PRINT) {
			generatePrintDeliveries(sub);
		}
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
