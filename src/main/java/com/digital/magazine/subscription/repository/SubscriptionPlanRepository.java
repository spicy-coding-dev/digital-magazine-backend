package com.digital.magazine.subscription.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.digital.magazine.subscription.entity.SubscriptionPlan;
import com.digital.magazine.subscription.enums.SubscriptionType;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    Optional<SubscriptionPlan> findByPlanCode(String planCode);

    List<SubscriptionPlan> findByTypeAndActiveTrueOrderByPriceAsc(SubscriptionType type);
    
}
