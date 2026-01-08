package com.digital.magazine.user.dto;

import com.digital.magazine.common.enums.ContentType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookContentDto {

	private int order;
	private ContentType type; // TEXT / IMAGE

	private String text;
	private String imageUrl;
}
