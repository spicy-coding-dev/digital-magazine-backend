package com.digital.magazine.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDto {
	@NotBlank(message = "கருத்து காலியாக இருக்கக்கூடாது.")
	@Size(max = 2000)
	private String content;
}
