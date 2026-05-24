package com.digital.magazine.common.response;

import com.digital.magazine.common.enums.Role;
import com.digital.magazine.subscription.dto.SubscriptionPopupDto;

public record LoginApiResponse(String accessToken, Role userRole, SubscriptionPopupDto subscriptionPopup) {

}
