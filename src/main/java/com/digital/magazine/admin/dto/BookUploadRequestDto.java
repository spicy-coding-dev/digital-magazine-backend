package com.digital.magazine.admin.dto;

import com.digital.magazine.common.enums.BookStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookUploadRequestDto {

	@NotBlank(message = "புத்தகத்தின் தலைப்பு அவசியம்")
	private String title;

	@NotBlank(message = "புத்தக வகை அவசியம்")
	private String category;
	
	@NotBlank(message = "புத்தகம் உருவாக்கியவர் அவசியம்")
	private String author;

	@NotNull(message = "paid/free தகவல் அவசியம்")
	private Boolean paid;

	@PositiveOrZero(message = "விலை 0 அல்லது அதற்கு மேல் இருக்க வேண்டும்")
	private Double price;

	@NotNull(message = "புத்தகம் நிலை அவசியம்")
	private BookStatus status;
}
