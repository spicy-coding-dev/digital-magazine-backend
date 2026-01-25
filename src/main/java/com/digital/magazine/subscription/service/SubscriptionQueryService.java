package com.digital.magazine.subscription.service;

import java.time.LocalDate;
import java.util.List;

import com.digital.magazine.subscription.dto.MagazinePurchaseAdminDto;
import com.digital.magazine.subscription.dto.SubscribedUserDto;
import com.digital.magazine.subscription.enums.SubscriptionStatus;
import com.digital.magazine.subscription.enums.SubscriptionType;

public interface SubscriptionQueryService {

	List<SubscribedUserDto> getSubscribedUsers(SubscriptionType type, SubscriptionStatus status);

	List<MagazinePurchaseAdminDto> getAllPurchases();

	List<MagazinePurchaseAdminDto> getPurchasesByBook(Long bookId);

	List<MagazinePurchaseAdminDto> getPurchasesBetweenDates(LocalDate fromDate, LocalDate toDate, Long bookId);
}
