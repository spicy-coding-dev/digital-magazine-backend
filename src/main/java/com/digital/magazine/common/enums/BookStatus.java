package com.digital.magazine.common.enums;

public enum BookStatus {
	DRAFT, PUBLISHED, BLOCKED;

	public static BookStatus fromString(String value) {
		if (value == null || value.isBlank()) {
			return PUBLISHED; // default
		}

		try {
			return BookStatus.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Invalid book status: " + value);
		}
	}
}
