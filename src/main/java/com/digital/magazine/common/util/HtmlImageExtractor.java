package com.digital.magazine.common.util;

import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HtmlImageExtractor {

	public static List<String> extractImageUrls(String html) {

		if (html == null || html.isBlank()) {
			return List.of();
		}

		Document doc = Jsoup.parse(html);

		return doc.select("img").stream().map(img -> img.attr("src")).filter(src -> src != null && !src.isBlank())
				.collect(Collectors.toList());
	}
}
