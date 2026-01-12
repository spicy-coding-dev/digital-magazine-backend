package com.digital.magazine.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.digital.magazine.subscription.entity.SubscriptionPlan;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.subscription.repository.SubscriptionPlanRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionInitializer {

	private final SubscriptionPlanRepository subscriptionRepo;

	@PostConstruct
	public void initSubscriptions() {

		if (subscriptionRepo.count() == 0) {

			subscriptionRepo.saveAll(List.of(

					SubscriptionPlan.builder().planCode("PRINT_1Y").name("ஆண்டு சந்தா").type(SubscriptionType.PRINT)
							.durationYears(1).price(BigDecimal.valueOf(800)).active(true).build(),

					SubscriptionPlan.builder().planCode("PRINT_3Y").name("மூன்றாண்டு சந்தா")
							.type(SubscriptionType.PRINT).durationYears(3).price(BigDecimal.valueOf(2200)).active(true)
							.build(),

					SubscriptionPlan.builder().planCode("PRINT_6Y").name("ஆறாண்டு சந்தா").type(SubscriptionType.PRINT)
							.durationYears(6).price(BigDecimal.valueOf(4200)).active(true).build(),

					SubscriptionPlan.builder().planCode("PRINT_LIFE").name("வாழ்நாள் சந்தா")
							.type(SubscriptionType.PRINT).durationYears(12).price(BigDecimal.valueOf(7000)).active(true)
							.build(),

					SubscriptionPlan.builder().planCode("PRINT_DONOR").name("புரவலர் நன்கொடை")
							.type(SubscriptionType.PRINT).durationYears(0).price(BigDecimal.valueOf(8000)).active(true)
							.build(),

					SubscriptionPlan.builder().planCode("DIGITAL_SINGLE").name("தனி இதழ்")
							.type(SubscriptionType.DIGITAL).durationYears(0).price(BigDecimal.valueOf(70)).active(true)
							.build(),

					SubscriptionPlan.builder().planCode("DIGITAL_3Y").name("மூன்றாண்டு டிஜிட்டல் சந்தா")
							.type(SubscriptionType.DIGITAL).durationYears(3).price(BigDecimal.valueOf(800)).active(true)
							.build(),

					SubscriptionPlan.builder().planCode("DIGITAL_6Y").name("ஆறாண்டு டிஜிட்டல் சந்தா")
							.type(SubscriptionType.DIGITAL).durationYears(6).price(BigDecimal.valueOf(1400))
							.active(true).build()));

			log.info("📦 Default subscription plans inserted");
		} else {
			log.info("✅ Default subscription already exists");
		}
	}

}

/*
 * INSERT INTO subscription_plans (plan_code, name, type, duration_years, price,
 * active) VALUES ('PRINT_1Y', 'ஆண்டு சந்தா', 'PRINT', 1, 800, true),
 * ('PRINT_3Y', 'மூன்றாண்டு சந்தா', 'PRINT', 3, 2200, true), ('PRINT_6Y',
 * 'ஆறாண்டு சந்தா', 'PRINT', 6, 4200, true), ('PRINT_LIFE', 'வாழ்நாள் சந்தா',
 * 'PRINT', 12, 7000, true), ('PRINT_DONOR', 'புரவலர் நன்கொடை', 'PRINT', 0,
 * 8000, true),
 * 
 * ('DIGITAL_SINGLE', 'தனி இதழ்', 'DIGITAL', 0, 70, true), ('DIGITAL_3Y',
 * 'மூன்றாண்டு சந்தா', 'DIGITAL', 3, 800, true), ('DIGITAL_6Y', 'ஆறாண்டு சந்தா',
 * 'DIGITAL', 6, 1400, true);
 */
