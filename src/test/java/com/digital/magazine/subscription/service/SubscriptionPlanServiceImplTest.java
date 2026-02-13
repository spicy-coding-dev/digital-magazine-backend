package com.digital.magazine.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.digital.magazine.common.exception.SubscriptionPlanNotFoundException;
import com.digital.magazine.subscription.dto.SubscriptionPlanDto;
import com.digital.magazine.subscription.dto.SubscriptionUpdateRequest;
import com.digital.magazine.subscription.entity.SubscriptionPlan;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.subscription.repository.SubscriptionPlanRepository;
import com.digital.magazine.subscription.service.impl.SubscriptionPlanServiceImpl;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanServiceImplTest {

	@Mock
	private SubscriptionPlanRepository repo;

	@InjectMocks
	private SubscriptionPlanServiceImpl service;

	/* ================= getActivePlans ================= */

	@Test
	void getActivePlans_success() {

		SubscriptionPlan digitalPlan = SubscriptionPlan.builder().planCode("DIGITAL_1Y").name("Digital 1 Year")
				.type(SubscriptionType.DIGITAL).durationYears(1).price(BigDecimal.valueOf(500)).active(true).build();

		SubscriptionPlan printPlan = SubscriptionPlan.builder().planCode("PRINT_1Y").name("Print 1 Year")
				.type(SubscriptionType.PRINT).durationYears(1).price(BigDecimal.valueOf(800)).active(true).build();

		when(repo.findByTypeAndActiveTrueOrderByPriceAsc(SubscriptionType.DIGITAL)).thenReturn(List.of(digitalPlan));

		when(repo.findByTypeAndActiveTrueOrderByPriceAsc(SubscriptionType.PRINT)).thenReturn(List.of(printPlan));

		Map<String, List<SubscriptionPlanDto>> result = service.getActivePlans();

		assertNotNull(result);
		assertEquals(2, result.size());

		assertEquals(1, result.get("digital").size());
		assertEquals("DIGITAL_1Y", result.get("digital").get(0).getPlanCode());

		assertEquals(1, result.get("print").size());
		assertEquals("PRINT_1Y", result.get("print").get(0).getPlanCode());

		verify(repo, times(1)).findByTypeAndActiveTrueOrderByPriceAsc(SubscriptionType.DIGITAL);

		verify(repo, times(1)).findByTypeAndActiveTrueOrderByPriceAsc(SubscriptionType.PRINT);
	}

	/* ================= updatePlan ================= */

	@Test
	void updatePlan_success() {

		SubscriptionPlan plan = SubscriptionPlan.builder().planCode("DIGITAL_1Y").name("Old Name")
				.type(SubscriptionType.DIGITAL).durationYears(1).price(BigDecimal.valueOf(500)).active(true).build();

		SubscriptionUpdateRequest request = new SubscriptionUpdateRequest();
		request.setName("Updated Name");
		request.setPrice(BigDecimal.valueOf(600));
		request.setDurationYears(2);

		when(repo.findByPlanCode("DIGITAL_1Y")).thenReturn(Optional.of(plan));

		when(repo.save(any(SubscriptionPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

		SubscriptionPlanDto dto = service.updatePlan("DIGITAL_1Y", request);

		assertNotNull(dto);
		assertEquals("DIGITAL_1Y", dto.getPlanCode());
		assertEquals("Updated Name", dto.getName());
		assertEquals(BigDecimal.valueOf(600), dto.getPrice());
		assertEquals(2, dto.getDurationYears());

		verify(repo).save(plan);
	}

	@Test
	void updatePlan_planNotFound_shouldThrowException() {

		when(repo.findByPlanCode("INVALID_PLAN")).thenReturn(Optional.empty());

		assertThrows(SubscriptionPlanNotFoundException.class,
				() -> service.updatePlan("INVALID_PLAN", new SubscriptionUpdateRequest()));

		verify(repo, never()).save(any());
	}
}
