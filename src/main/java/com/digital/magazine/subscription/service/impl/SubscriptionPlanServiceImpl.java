package com.digital.magazine.subscription.service.impl;

import java.util.*;

import org.springframework.stereotype.Service;

import com.digital.magazine.common.exception.SubscriptionPlanNotFoundException;
import com.digital.magazine.subscription.dto.SubscriptionPlanDto;
import com.digital.magazine.subscription.dto.SubscriptionUpdateRequest;
import com.digital.magazine.subscription.entity.SubscriptionPlan;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.subscription.repository.SubscriptionPlanRepository;
import com.digital.magazine.subscription.service.SubscriptionPlanService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

	private final SubscriptionPlanRepository repo;

	@Override
	public Map<String, List<SubscriptionPlanDto>> getActivePlans() {

		log.info("🔍 Fetching active subscription plans");

		Map<String, List<SubscriptionPlanDto>> result = new LinkedHashMap<>();

		for (SubscriptionType type : SubscriptionType.values()) {

			log.debug("➡️ Fetching plans for type={}", type);

			List<SubscriptionPlanDto> plans = repo.findByTypeAndActiveTrueOrderByPriceAsc(type).stream()
					.map(this::toDto).toList();

			log.info("✅ Found {} active plans for type={}", plans.size(), type);

			result.put(type.name().toLowerCase(), plans);
		}

		log.info("🎉 Subscription plan aggregation completed");

		return result;
	}

	@Override
	public SubscriptionPlanDto updatePlan(String planCode, SubscriptionUpdateRequest request) {

		SubscriptionPlan plan = repo.findByPlanCode(planCode)
				.orElseThrow(() -> new SubscriptionPlanNotFoundException("சந்தா திட்டம் கிடைக்கவில்லை"));

		if (request.getPlanCode() != null)
			plan.setPlanCode(request.getPlanCode());

		if (request.getName() != null)
			plan.setName(request.getName());

		if (request.getPrice() != null)
			plan.setPrice(request.getPrice());

		if (request.getType() != null)
			plan.setType(request.getType());

		if (request.getDurationYears() != null)
			plan.setDurationYears(request.getDurationYears());

		if (request.getActive() != null)
			plan.setActive(request.getActive());

		repo.save(plan);

		log.info("✅ Subscription plan updated: {}", planCode);

		return toDto(plan);
	}

	private SubscriptionPlanDto toDto(SubscriptionPlan plan) {
		return SubscriptionPlanDto.builder().planId(plan.getId()).planCode(plan.getPlanCode()).name(plan.getName())
				.type(plan.getType()).durationYears(plan.getDurationYears()).price(plan.getPrice()).build();
	}
}
