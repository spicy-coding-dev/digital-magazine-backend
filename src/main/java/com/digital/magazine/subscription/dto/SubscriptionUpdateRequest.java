package com.digital.magazine.subscription.dto;

import java.math.BigDecimal;

import com.digital.magazine.subscription.enums.SubscriptionType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionUpdateRequest {
	
	private String planCode;
	private String name;
	private SubscriptionType type;
    private BigDecimal price;
    private Integer durationYears;
    private Boolean active;

}
