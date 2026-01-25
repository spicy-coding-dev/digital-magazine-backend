package com.digital.magazine.subscription.controller;

import java.time.LocalDate;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.subscription.dto.UpdatePrintIssueRequest;
import com.digital.magazine.subscription.service.PrintDeliveryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminPrintIssueController {

	private final PrintDeliveryService service;

	// 🔍 Get all / by status
	@GetMapping("/print/issues")
	public ResponseEntity<?> getIssues(@RequestParam Long id) {

		log.info("📥 GET print issues | subscriptionId={}", id);

		return ResponseEntity.ok(service.getPrintIssues(id));
	}

	@PutMapping("/update/{deliveryId}")
	public ResponseEntity<?> updateIssue(@PathVariable Long deliveryId, @RequestBody UpdatePrintIssueRequest req,
			Authentication auth) {

		service.updatePrintIssueByMagazine(deliveryId, req, auth);
		return ResponseEntity.ok("Status updated successfully");
	}

	@GetMapping("/print/shipped/today")
	public ResponseEntity<byte[]> printTodayShipped() {

		byte[] pdf = service.generateTodayShippedInvoices();

		if (pdf == null || pdf.length == 0) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=shipped_" + LocalDate.now() + ".pdf")
				.contentType(MediaType.APPLICATION_PDF).body(pdf);
	}

}
