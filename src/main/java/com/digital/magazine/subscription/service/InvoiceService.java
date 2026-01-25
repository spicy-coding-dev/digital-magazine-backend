package com.digital.magazine.subscription.service;

import java.util.List;

import com.digital.magazine.subscription.entity.PrintDelivery;

public interface InvoiceService {

	public byte[] generatePrintInvoice(List<PrintDelivery> deliveries);

}
