package com.digital.magazine.subscription.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.digital.magazine.subscription.entity.UserSubscription;
import com.digital.magazine.subscription.enums.SubscriptionStatus;
import com.digital.magazine.subscription.repository.UserSubscriptionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpiryScheduler {

	private final UserSubscriptionRepository subscriptionRepo;

	/**
	 * Runs every day at 12:05 AM
	 */
	@Scheduled(cron = "0 5 0 * * ?", zone = "Asia/Kolkata")
	public void expireSubscriptions() {

		log.info("⏰ Subscription expiry scheduler started");

		LocalDate today = LocalDate.now();

		List<UserSubscription> expiredSubs = subscriptionRepo.findByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE,
				today);

		if (expiredSubs.isEmpty()) {
			log.info("✅ No subscriptions to expire today");
			return;
		}

		for (UserSubscription sub : expiredSubs) {
			sub.setStatus(SubscriptionStatus.EXPIRED);
			log.info("🚫 Subscription expired | userId={} | plan={} | endDate={}", sub.getUser().getId(),
					sub.getPlan().getPlanCode(), sub.getEndDate());
		}

		subscriptionRepo.saveAll(expiredSubs);

		log.info("✅ Total expired subscriptions updated = {}", expiredSubs.size());
	}
}
