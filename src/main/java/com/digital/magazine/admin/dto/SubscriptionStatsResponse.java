package com.digital.magazine.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionStatsResponse {

	private long freeUsers;
	private long paidUsers;
	private long expiringSoon;
}
