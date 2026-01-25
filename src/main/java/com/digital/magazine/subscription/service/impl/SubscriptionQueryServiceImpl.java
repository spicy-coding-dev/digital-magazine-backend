package com.digital.magazine.subscription.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.subscription.dto.MagazinePurchaseAdminDto;
import com.digital.magazine.subscription.dto.SubscribedUserDto;
import com.digital.magazine.subscription.dto.UserAddressResponseDto;
import com.digital.magazine.subscription.entity.MagazinePurchase;
import com.digital.magazine.subscription.entity.UserAddress;
import com.digital.magazine.subscription.entity.UserSubscription;
import com.digital.magazine.subscription.enums.SubscriptionStatus;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.subscription.repository.MagazinePurchaseRepository;
import com.digital.magazine.subscription.repository.UserSubscriptionRepository;
import com.digital.magazine.subscription.service.SubscriptionQueryService;

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
