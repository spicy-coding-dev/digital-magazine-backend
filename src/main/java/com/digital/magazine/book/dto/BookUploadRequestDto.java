package com.digital.magazine.book.dto;

import java.util.List;

import com.digital.magazine.common.enums.BookStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BookUploadRequestDto {

	@NotBlank(message = "புத்தகத்தின் தலைப்பு அவசியம்")
	private String title;

	@NotBlank(message = "புத்தகத்தின் துணைத்தலைப்பு அவசியம்")
	private String subtitle;

	@NotBlank(message = "புத்தக வகை அவசியம்")
	private String category;

	@NotBlank(message = "புத்தகம் உருவாக்கியவர் அவசியம்")
	private String author;

	@NotNull(message = "paid/free தகவல் அவசியம்")
	private Boolean paid;

	@PositiveOrZero(message = "விலை 0 அல்லது அதற்கு மேல் இருக்க வேண்டும்")
	private Double price;

	@NotNull(message = "Magazine number அவசியம்")
	@PositiveOrZero(message = "Magazine number 0 அல்லது அதற்கு மேல் இருக்க வேண்டும்")
	private Long magazineNo;

	@NotEmpty(message = "குறைந்தது ஒரு tag அவசியம்")
	private List<String> tags;

	// example: "tamil,history,politics"

	@NotNull(message = "புத்தகம் நிலை அவசியம்")
	private BookStatus status;
}
