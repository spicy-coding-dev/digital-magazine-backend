package com.digital.magazine.analytics.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArticleViewDto {
	private Long articleId;
	private Long timeSpentSeconds;
}
