package com.digital.magazine.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.digital.magazine.common.exception.*;
import com.digital.magazine.subscription.dto.BuySubscriptionRequest;
import com.digital.magazine.subscription.entity.*;
import com.digital.magazine.subscription.enums.*;
import com.digital.magazine.subscription.repository.*;
import com.digital.magazine.subscription.service.impl.SubscriptionServiceImpl;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

	@Mock
	private UserSubscriptionRepository userSubscriptionRepo;
	@Mock
	private PrintDeliveryRepository printDeliveryRepo;
	@Mock
	private UserAddressRepository addressRepo;
	@Mock
	private SubscriptionPlanRepository subscriptionPlanRepo;
	@Mock
	private UserRepository userRepo;
	@Mock
	private Authentication auth;

	@InjectMocks
	private SubscriptionServiceImpl service;

	private User user;
	private SubscriptionPlan digitalPlan;
	private SubscriptionPlan printPlan;

	@BeforeEach
	void setup() {

		user = User.builder().id(1L).email("user@test.com").build();

		digitalPlan = SubscriptionPlan.builder().id(1L).name("Digital 1Y").type(SubscriptionType.DIGITAL)
				.durationYears(1).price(BigDecimal.valueOf(500)).build();

		printPlan = SubscriptionPlan.builder().id(2L).name("Print 1Y").type(SubscriptionType.PRINT).durationYears(1)
				.price(BigDecimal.valueOf(800)).build();

		when(auth.getName()).thenReturn("user@test.com");
		when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.of(user));
	}

	/* ================= SUCCESS ================= */

	@Test
	void buy_digitalSubscription_success() {

		BuySubscriptionRequest req = new BuySubscriptionRequest();
		req.setPlanId(1L);

		when(subscriptionPlanRepo.findById(1L)).thenReturn(Optional.of(digitalPlan));
		when(userSubscriptionRepo.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)).thenReturn(Optional.empty());

		String result = service.buy(req, auth);

		assertTrue(result.contains("Digital 1Y"));
		verify(userSubscriptionRepo).save(any(UserSubscription.class));
		verify(printDeliveryRepo, never()).save(any());
	}

	@Test
	void buy_printSubscription_success() {

		UserAddress address = UserAddress.builder().id(10L).user(user).build();

		BuySubscriptionRequest req = new BuySubscriptionRequest();
		req.setPlanId(2L);
		req.setAddressId(10L);

		when(subscriptionPlanRepo.findById(2L)).thenReturn(Optional.of(printPlan));
		when(userSubscriptionRepo.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)).thenReturn(Optional.empty());
		when(addressRepo.findByIdAndUser(10L, user)).thenReturn(Optional.of(address));

		String result = service.buy(req, auth);

		assertTrue(result.contains("Print 1Y"));
		verify(printDeliveryRepo, times(6)).save(any(PrintDelivery.class)); // 1 year = 6 issues
	}

	/* ================= RULE FAILURES ================= */

	@Test
	void buy_digitalSingle_notAllowed() {

		digitalPlan.setDurationYears(0);

		BuySubscriptionRequest req = new BuySubscriptionRequest();
		req.setPlanId(1L);

		when(subscriptionPlanRepo.findById(1L)).thenReturn(Optional.of(digitalPlan));

		assertThrows(SubscriptionNotAllowedException.class, () -> service.buy(req, auth));
	}

	@Test
	void buy_duplicateSubscription_shouldFail() {

		UserSubscription existing = UserSubscription.builder().user(user).plan(digitalPlan)
				.status(SubscriptionStatus.ACTIVE).endDate(LocalDate.now().plusMonths(5)).build();

		BuySubscriptionRequest req = new BuySubscriptionRequest();
		req.setPlanId(1L);

		when(subscriptionPlanRepo.findById(1L)).thenReturn(Optional.of(digitalPlan));
		when(userSubscriptionRepo.findByUserAndStatus(user, SubscriptionStatus.ACTIVE))
				.thenReturn(Optional.of(existing));

		assertThrows(DuplicateSubscriptionException.class, () -> service.buy(req, auth));
	}

	@Test
	void buy_digitalWithAddress_shouldFail() {

		BuySubscriptionRequest req = new BuySubscriptionRequest();
		req.setPlanId(1L);
		req.setAddressId(99L);

		when(subscriptionPlanRepo.findById(1L)).thenReturn(Optional.of(digitalPlan));
		when(userSubscriptionRepo.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)).thenReturn(Optional.empty());

		assertThrows(AddressNotRequiredException.class, () -> service.buy(req, auth));
	}

	@Test
	void buy_printWithoutAddress_shouldFail() {

		BuySubscriptionRequest req = new BuySubscriptionRequest();
		req.setPlanId(2L);

		when(subscriptionPlanRepo.findById(2L)).thenReturn(Optional.of(printPlan));
		when(userSubscriptionRepo.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)).thenReturn(Optional.empty());
		when(addressRepo.findByUserAndDefaultAddressTrue(user)).thenReturn(Optional.empty());

		assertThrows(AddressRequiredException.class, () -> service.buy(req, auth));
	}

	@Test
	void buy_printWithForeignAddress_shouldFail() {

		BuySubscriptionRequest req = new BuySubscriptionRequest();
		req.setPlanId(2L);
		req.setAddressId(99L);

		when(subscriptionPlanRepo.findById(2L)).thenReturn(Optional.of(printPlan));
		when(userSubscriptionRepo.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)).thenReturn(Optional.empty());
		when(addressRepo.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

		assertThrows(AddressAccessDeniedException.class, () -> service.buy(req, auth));
	}
}
