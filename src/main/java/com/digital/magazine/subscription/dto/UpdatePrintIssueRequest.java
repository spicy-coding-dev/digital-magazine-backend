package com.digital.magazine.subscription.dto;

import java.time.LocalDate;

import com.digital.magazine.subscription.enums.DeliveryStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePrintIssueRequest {

	@NotNull(message = "இதழ் எண் (Magazine No) கட்டாயம் தேவை")
	@Positive(message = "இதழ் எண் சரியான எண்ணாக இருக்க வேண்டும்")
	private Long magazineNo;
	@NotBlank(message = "கூரியர் நிறுவனத்தின் பெயர் கட்டாயம்")
	private String courierName;
	@NotNull(message = "விநியோக நிலை (Delivery Status) கட்டாயம்")
	private DeliveryStatus status; // ISSUED / DELIVERED
	private LocalDate deliveryDate;
}
