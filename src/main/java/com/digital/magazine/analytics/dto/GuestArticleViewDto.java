package com.digital.magazine.analytics.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuestArticleViewDto {
	private String guestId;
	private Long articleId;
	private Long timeSpentSeconds;
}
