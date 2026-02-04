package com.digital.magazine.book.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReplyRequestDto {
	@NotBlank
	private String reply;
}
