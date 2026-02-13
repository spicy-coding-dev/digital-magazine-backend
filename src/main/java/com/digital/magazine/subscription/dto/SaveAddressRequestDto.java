package com.digital.magazine.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SaveAddressRequestDto {

	@NotBlank(message = "முகவரி பெயர் அவசியம் (உதா: வீடு / அலுவலகம்)")
	private String name;

	@NotBlank(message = "முகவரி விவரம் காலியாக இருக்கக் கூடாது")
	@Size(min = 5, message = "முகவரி குறைந்தது 5 எழுத்துகள் இருக்க வேண்டும்")
	private String addressLine;

	@NotBlank(message = "நகரத்தின் பெயர் அவசியம்")
	private String city;

	@NotBlank(message = "மாநிலத்தின் பெயர் அவசியம்")
	private String state;

	@NotBlank(message = "PIN code அவசியம்")
	@Pattern(regexp = "^[0-9]{6}$", message = "PIN code 6 இலக்க எண்ணாக இருக்க வேண்டும்")
	private String pincode;

	@NotBlank(message = "மொபைல் எண் அவசியம்")
	@Pattern(regexp = "^[6-9][0-9]{9}$", message = "சரியான இந்திய மொபைல் எண்ணை உள்ளிடவும்")
	private String mobile;

	private boolean defaultAddress;
}
