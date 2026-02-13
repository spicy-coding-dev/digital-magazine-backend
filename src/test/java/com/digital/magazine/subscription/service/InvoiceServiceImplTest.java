package com.digital.magazine.subscription.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.subscription.entity.PrintDelivery;
import com.digital.magazine.subscription.entity.SubscriptionPlan;
import com.digital.magazine.subscription.entity.UserAddress;
import com.digital.magazine.subscription.entity.UserSubscription;
import com.digital.magazine.subscription.enums.DeliveryStatus;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.subscription.service.impl.InvoiceServiceImpl;
import com.digital.magazine.user.entity.User;

class InvoiceServiceImplTest {

	private InvoiceServiceImpl invoiceService;

	@BeforeEach
	void setup() {
		invoiceService = new InvoiceServiceImpl();
	}

	// ❌ null deliveries
	@Test
	void generatePrintInvoice_nullList_shouldReturnEmptyBytes() {

		byte[] result = invoiceService.generatePrintInvoice(null);

		assertNotNull(result);
		assertEquals(0, result.length);
	}

	// ❌ empty deliveries
	@Test
	void generatePrintInvoice_emptyList_shouldReturnEmptyBytes() {

		byte[] result = invoiceService.generatePrintInvoice(List.of());

		assertNotNull(result);
		assertEquals(0, result.length);
	}

	// ✅ single delivery
	@Test
	void generatePrintInvoice_singleDelivery_success() {

		List<PrintDelivery> deliveries = List.of(mockDelivery());

		byte[] pdf = invoiceService.generatePrintInvoice(deliveries);

		assertNotNull(pdf);
		assertTrue(pdf.length > 0, "PDF byte array should not be empty");
	}

	// ✅ multiple deliveries (new page logic)
	@Test
	void generatePrintInvoice_multipleDeliveries_success() {

		List<PrintDelivery> deliveries = List.of(mockDelivery(), mockDelivery(), mockDelivery(), mockDelivery(),
				mockDelivery() // >4 → new page
		);

		byte[] pdf = invoiceService.generatePrintInvoice(deliveries);

		assertNotNull(pdf);
		assertTrue(pdf.length > 0);
	}

	// -------------------------------------------------
	// 🔧 Helper method to create full PrintDelivery graph
	// -------------------------------------------------

	private PrintDelivery mockDelivery() {

		User user = User.builder().id(1L).name("Test User").email("user@test.com").build();

		UserAddress address = UserAddress.builder().name("Home").addressLine("Anna Nagar").city("Madurai")
				.pincode("625001").mobile("9876543210").build();

		SubscriptionPlan plan = SubscriptionPlan.builder().type(SubscriptionType.PRINT).durationYears(1).build();

		UserSubscription subscription = UserSubscription.builder().id(10L).user(user).plan(plan)
				.deliveryAddress(address).build();

		Books book = Books.builder().id(100L).title("Tamil Magazine").magazineNo(5L).build();

		return PrintDelivery.builder().id(50L).subscription(subscription).book(book).courierName("DTDC")
				.issuedBy("Admin").deliveryDate(LocalDate.now()).status(DeliveryStatus.SHIPPED).build();
	}
}
