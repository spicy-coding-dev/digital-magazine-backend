package com.digital.magazine.subscription.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.subscription.dto.MagazinePurchaseAdminDto;
import com.digital.magazine.subscription.dto.SubscribedUserDto;
import com.digital.magazine.subscription.dto.SubscriptionPopupDto;
import com.digital.magazine.subscription.dto.UserAddressResponseDto;
import com.digital.magazine.subscription.entity.MagazinePurchase;
import com.digital.magazine.subscription.entity.UserAddress;
import com.digital.magazine.subscription.entity.UserSubscription;
import com.digital.magazine.subscription.enums.SubscriptionStatus;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.subscription.repository.MagazinePurchaseRepository;
import com.digital.magazine.subscription.repository.UserSubscriptionRepository;
import com.digital.magazine.subscription.service.SubscriptionQueryService;
import com.digital.magazine.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionQueryServiceImpl implements SubscriptionQueryService {

	private final UserSubscriptionRepository subscriptionRepo;
	private final MagazinePurchaseRepository purchaseRepo;
	private final BookRepository bookRepo;

	@Override
	public List<SubscribedUserDto> getSubscribedUsers(SubscriptionType type, SubscriptionStatus status) {

		log.info("📥 Fetch subscribed users | type={} | status={}", type, status);

		List<UserSubscription> subs = (type != null) ? subscriptionRepo.findByPlan_TypeAndStatus(type, status)
				: subscriptionRepo.findByStatus(status);

		log.info("📊 Subscriptions fetched count={}", subs.size());

		return subs.stream().map(this::mapToSubscribedUserDto).collect(Collectors.toList());

	}

	@Override
	public List<MagazinePurchaseAdminDto> getAllPurchases() {

		log.info("📊 Admin fetching ALL single magazine purchases");

		List<MagazinePurchase> purchases = purchaseRepo.findAllByOrderByPurchasedAtDesc();

		log.info("📦 Total purchases found={}", purchases.size());

		return purchases.stream().map(this::toDto).toList();
	}

	@Override
	public List<MagazinePurchaseAdminDto> getPurchasesByBook(Long bookId) {

		log.info("📘 Admin fetching purchases for bookId={}", bookId);

		Books book = bookRepo.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));

		List<MagazinePurchase> purchases = purchaseRepo.findByBookOrderByPurchasedAtDesc(book);

		log.info("📦 Purchases found for bookId={} → count={}", bookId, purchases.size());

		return purchases.stream().map(this::toDto).toList();
	}

	@Override
	public List<MagazinePurchaseAdminDto> getPurchasesBetweenDates(LocalDate fromDate, LocalDate toDate, Long bookId) {

		LocalDateTime from = fromDate.atStartOfDay();
		LocalDateTime to = toDate.atTime(LocalTime.MAX);

		log.info("📅 Fetch purchases | from={} | to={} | bookId={}", from, to, bookId);

		List<MagazinePurchase> purchases;

		if (bookId != null) {

			Books book = bookRepo.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));

			purchases = purchaseRepo.findByBookAndPurchasedAtBetweenOrderByPurchasedAtDesc(book, from, to);

			log.info("📘 Purchases for bookId={} → count={}", bookId, purchases.size());

		} else {

			purchases = purchaseRepo.findByPurchasedAtBetweenOrderByPurchasedAtDesc(from, to);

			log.info("📦 Total purchases in date range → count={}", purchases.size());
		}

		return purchases.stream().map(this::toDto).toList();
	}

	public SubscriptionPopupDto getSubscriptionPopup(User user) {

		List<UserSubscription> subscriptions = subscriptionRepo.findByUserAndStatusIn(user,
				List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.EXPIRING_SOON, SubscriptionStatus.EXPIRED));

		if (subscriptions.isEmpty()) {

			return SubscriptionPopupDto.builder().show(false).build();
		}

		UserSubscription sub = subscriptions.stream().max(Comparator.comparing(UserSubscription::getEndDate))
				.orElseThrow();

		/* 🔥 EXPIRING SOON */
		if (sub.getStatus() == SubscriptionStatus.EXPIRING_SOON) {

			return SubscriptionPopupDto.builder()

					.show(true)

					.status("EXPIRING_SOON")

					.message("உங்கள் " + sub.getPlan().getName() + " விரைவில் " + sub.getEndDate()
							+ " அன்று முடிவடைகிறது. தயவுசெய்து புதுப்பிக்கவும்.")

					.endDate(sub.getEndDate())

					.build();

		} else if (sub.getStatus() == SubscriptionStatus.EXPIRED) {

			return SubscriptionPopupDto.builder()

					.show(true)

					.status("EXPIRED")

					.message("உங்கள் " + sub.getPlan().getName() + " " + sub.getEndDate()
							+ " அன்று காலாவதியானது. தொடர்ந்து பயன்படுத்த சந்தாவை புதுப்பிக்கவும்.")

					.endDate(sub.getEndDate())

					.build();

		}

		/* 🔥 ACTIVE */
		return SubscriptionPopupDto.builder()

				.show(true)

				.status("ACTIVE")

				.message("உங்கள் " + sub.getPlan().getName() + " " + sub.getStartDate() + " முதல் " + sub.getEndDate()
						+ " வரை செயல்பாட்டில் இருக்கும்")

				.endDate(sub.getEndDate())

				.build();
	}

	private SubscribedUserDto mapToSubscribedUserDto(UserSubscription s) {

		UserAddressResponseDto addressDto = null;

		if (s.getPlan().getType() == SubscriptionType.PRINT && s.getDeliveryAddress() != null) {

			UserAddress a = s.getDeliveryAddress();

			addressDto = UserAddressResponseDto.builder().name(a.getName()).addressLine(a.getAddressLine())
					.city(a.getCity()).state(a.getState()).pincode(a.getPincode()).mobile(a.getMobile()).build();
		}

		return SubscribedUserDto.builder().userId(s.getUser().getId()).name(s.getUser().getName())
				.email(s.getUser().getEmail()).planName(s.getPlan().getName()).planType(s.getPlan().getType())
				.startDate(s.getStartDate()).endDate(s.getEndDate()).status(s.getStatus()).address(addressDto)
				.subscriptionId(s.getId()).build();
	}

	private MagazinePurchaseAdminDto toDto(MagazinePurchase p) {

		return MagazinePurchaseAdminDto.builder().purchaseId(p.getId())

				.userId(p.getUser().getId()).userName(p.getUser().getName()).userEmail(p.getUser().getEmail())
				.mobile(p.getUser().getMobile())

				.bookTitle(p.getBook().getTitle()).magazineNo(p.getBook().getMagazineNo())

				.price(p.getPrice()).purchasedAt(p.getPurchasedAt()).build();
	}

}
