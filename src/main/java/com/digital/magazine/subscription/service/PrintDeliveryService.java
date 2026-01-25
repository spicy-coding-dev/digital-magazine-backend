package com.digital.magazine.subscription.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.digital.magazine.subscription.dto.PrintDeliveryResponseDto;
import com.digital.magazine.subscription.dto.UpdatePrintIssueRequest;

public interface PrintDeliveryService {

	List<PrintDeliveryResponseDto> getPrintIssues(Long subscriptionId);

	public void updatePrintIssueByMagazine(Long deliveryId, UpdatePrintIssueRequest req, Authentication auth);

	public byte[] generateTodayShippedInvoices();
}
