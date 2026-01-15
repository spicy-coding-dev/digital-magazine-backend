package com.digital.magazine.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.digital.magazine.subscription.entity.UserSubscription;
import com.digital.magazine.subscription.enums.SubscriptionStatus;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.user.entity.User;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

//	boolean existsActiveDigital(Long id);

	boolean existsByUserAndPlan_TypeAndStatus(User user, SubscriptionType type, SubscriptionStatus status);

}
