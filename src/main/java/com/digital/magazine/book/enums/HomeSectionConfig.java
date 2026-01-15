package com.digital.magazine.book.enums;

import com.digital.magazine.common.enums.BookCategory;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HomeSectionConfig {

	// 🔥 SECTION 1: Latest from ALL categories
	LATEST_ALL("Latest", null, 6),

	// 🔥 SECTION 2: Category-wise
	HISTORY("வரலாறு", BookCategory.HISTORY, 2), SOCIETY("சமூகம்", BookCategory.SOCIETY, 4),
	LITERATURE("இலக்கியம்", BookCategory.LITERATURE, 3), CULTURE("பண்பாடு", BookCategory.CULTURE, 2);

	private final String title; // UI title
	private final BookCategory category; // null = ALL
	private final int limit; // how many to show
}
