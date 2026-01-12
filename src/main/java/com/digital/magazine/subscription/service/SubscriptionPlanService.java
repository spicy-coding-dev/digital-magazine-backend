package com.digital.magazine.subscription.service;

import java.util.Map;
import java.util.List;

import com.digital.magazine.subscription.dto.SubscriptionPlanDto;
import com.digital.magazine.subscription.dto.SubscriptionUpdateRequest;

public interface SubscriptionPlanService {

    Map<String, List<SubscriptionPlanDto>> getActivePlans();
    
    SubscriptionPlanDto updatePlan(String planCode, SubscriptionUpdateRequest request);
}
