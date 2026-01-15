package com.digital.magazine.subscription.service;

import org.springframework.security.core.Authentication;

import com.digital.magazine.subscription.dto.BuySubscriptionRequest;
import com.digital.magazine.subscription.entity.SubscriptionPlan;
import com.digital.magazine.subscription.entity.UserAddress;
import com.digital.magazine.user.entity.User;

public interface SubscriptionService {

	public void buy(BuySubscriptionRequest req, Authentication auth);

	public void activateSubscription(User user, SubscriptionPlan plan, UserAddress address);

}
