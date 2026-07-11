package com.digital.magazine.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReplyRequestDto {
	@NotBlank(message = "பதில் காலியாக இருக்கக்கூடாது.")
	@Size(max = 2000)
	private String reply;
}
