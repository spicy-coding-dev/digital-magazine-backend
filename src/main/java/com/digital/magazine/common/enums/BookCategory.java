package com.digital.magazine.common.enums;

public enum BookCategory {

	HISTORY("வரலாறு"), SOCIETY("சமூகம்"), LITERATURE("இலக்கியம்"), CULTURE("பண்பாடு"), ENVIRONMENT("சூழலியல்"),
	EDITORIAL("தலையங்கம்"), REVIEW("தலையங்கம்"), MAGAZINE("இதழ்கள்");

	private final String tamilLabel;

	BookCategory(String tamilLabel) {
		this.tamilLabel = tamilLabel;
	}

	public String getTamilLabel() {
		return tamilLabel;
	}

	// 🔁 Tamil → Enum (API input)
	public static BookCategory fromTamil(String tamil) {
		for (BookCategory c : values()) {
			if (c.tamilLabel.equals(tamil)) {
				return c;
			}
		}
		throw new IllegalArgumentException("Invalid category: " + tamil);
	}
}
