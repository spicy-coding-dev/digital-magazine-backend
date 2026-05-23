package com.digital.magazine.subscription.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.digital.magazine.common.service.EmailService;
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
	private final EmailService emailService;

	@Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Kolkata")
//	@Scheduled(cron = "0 25 15 * * ?", zone = "Asia/Kolkata")
	public void markExpiringSoonSubscriptions() {

		log.info("⏰ Expiring soon scheduler started");

		LocalDate today = LocalDate.now();

		LocalDate targetDate = today.plusDays(5);

		List<UserSubscription> subs = subscriptionRepo.findByStatusAndEndDate(SubscriptionStatus.ACTIVE, targetDate);

		if (subs.isEmpty()) {

			log.info("✅ No subscriptions nearing expiry");

			return;
		}

		for (UserSubscription sub : subs) {

			sub.setStatus(SubscriptionStatus.EXPIRING_SOON);

			log.info("⚠️ Subscription marked EXPIRING_SOON | userId={} | endDate={}", sub.getUser().getId(),
					sub.getEndDate());

			emailService.sendSubscriptionExpiringSoonEmail(sub.getUser().getEmail(), sub.getUser().getName(),
					sub.getPlan().getName(), 5L, sub.getEndDate());

			log.info("📧 Expiring soon reminder mail sent | user={} | plan={}", sub.getUser().getEmail(),
					sub.getPlan().getName());

		}

		subscriptionRepo.saveAll(subs);

		log.info("✅ Total EXPIRING_SOON subscriptions updated = {}", subs.size());
	}

	@Scheduled(cron = "0 10 0 * * ?", zone = "Asia/Kolkata")
//	@Scheduled(cron = "0 26 15 * * ?", zone = "Asia/Kolkata")
	public void expireSubscriptions() {

		log.info("⏰ Subscription expiry scheduler started");

		LocalDate today = LocalDate.now();

		List<UserSubscription> expiredSubs = subscriptionRepo.findByStatusInAndEndDateBefore(
				List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.EXPIRING_SOON), today);

		if (expiredSubs.isEmpty()) {

			log.info("✅ No subscriptions to expire today");

			return;
		}

		for (UserSubscription sub : expiredSubs) {

			sub.setStatus(SubscriptionStatus.EXPIRED);

			log.info("🚫 Subscription expired | userId={} | endDate={}", sub.getUser().getId(), sub.getEndDate());

			emailService.sendSubscriptionExpiredEmail(sub.getUser().getEmail(), sub.getUser().getName(),
					sub.getPlan().getName(), sub.getEndDate());
		}

		subscriptionRepo.saveAll(expiredSubs);

		log.info("✅ Total expired subscriptions updated = {}", expiredSubs.size());
	}

}
