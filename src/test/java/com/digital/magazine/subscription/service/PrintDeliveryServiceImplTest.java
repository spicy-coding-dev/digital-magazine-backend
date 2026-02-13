package com.digital.magazine.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.common.enums.BookCategory;
import com.digital.magazine.common.exception.*;
import com.digital.magazine.subscription.dto.UpdatePrintIssueRequest;
import com.digital.magazine.subscription.entity.*;
import com.digital.magazine.subscription.enums.DeliveryStatus;
import com.digital.magazine.subscription.repository.PrintDeliveryRepository;
import com.digital.magazine.subscription.service.impl.PrintDeliveryServiceImpl;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PrintDeliveryServiceImplTest {

	@Mock
	private UserRepository userRepo;

	@Mock
	private BookRepository bookRepo;

	@Mock
	private PrintDeliveryRepository deliveryRepo;

	@Mock
	private InvoiceService invoiceService;

	@Mock
	private Authentication authentication;

	@InjectMocks
	private PrintDeliveryServiceImpl service;

	private User user;
	private Books book;
	private PrintDelivery delivery;

	@BeforeEach
	void setup() {

		user = User.builder().id(1L).name("Admin").email("admin@test.com").build();

		book = Books.builder().id(10L).title("Print Magazine").magazineNo(2L)
				.category(BookCategory.fromTamil("இதழ்கள்")).build();

		UserAddress address = UserAddress.builder().name("User").addressLine("Street 1").city("Chennai").state("TN")
				.pincode("600001").mobile("9999999999").build();

		UserSubscription sub = UserSubscription.builder().id(100L).user(user).deliveryAddress(address).build();

		delivery = PrintDelivery.builder().id(50L).subscription(sub).status(DeliveryStatus.PENDING)
				.deliveryDate(LocalDate.now()).build();
	}

	// ✅ getPrintIssues
	@Test
	void getPrintIssues_success() {

		when(deliveryRepo.findBySubscriptionId(100L)).thenReturn(List.of(delivery));

		var result = service.getPrintIssues(100L);

		assertEquals(1, result.size());
		verify(deliveryRepo).findBySubscriptionId(100L);
	}

	// ✅ updatePrintIssue SUCCESS
	@Test
	void updatePrintIssue_success() {

		UpdatePrintIssueRequest req = UpdatePrintIssueRequest.builder().magazineNo(2L).courierName("DTDC")
				.status(DeliveryStatus.SHIPPED).deliveryDate(LocalDate.now()).build();

		// 🔥 THIS WAS MISSING
		when(authentication.getName()).thenReturn("admin@test.com");

		when(userRepo.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

		when(bookRepo.findByCategoryAndMagazineNo(any(), eq(2L))).thenReturn(Optional.of(book));

		when(deliveryRepo.findById(50L)).thenReturn(Optional.of(delivery));

		service.updatePrintIssueByMagazine(50L, req, authentication);

		verify(deliveryRepo).save(delivery);
		assertEquals(DeliveryStatus.SHIPPED, delivery.getStatus());
	}

	// ❌ User not found
	@Test
	void updatePrintIssue_userNotFound() {

		// 🔥 MUST: auth must return something
		when(authentication.getName()).thenReturn("admin@test.com");

		// user does NOT exist
		when(userRepo.findByEmail("admin@test.com")).thenReturn(Optional.empty());

		UpdatePrintIssueRequest req = UpdatePrintIssueRequest.builder().magazineNo(202602L).courierName("DTDC")
				.status(DeliveryStatus.SHIPPED).build();

		assertThrows(UserNotFoundException.class, () -> service.updatePrintIssueByMagazine(1L, req, authentication));
	}

	// ❌ Book not found
	@Test
	void updatePrintIssue_bookNotFound() {

		// 🔥 REQUIRED
		when(authentication.getName()).thenReturn("admin@test.com");

		when(userRepo.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

		when(bookRepo.findByCategoryAndMagazineNo(any(), any())).thenReturn(Optional.empty());

		UpdatePrintIssueRequest req = UpdatePrintIssueRequest.builder().magazineNo(2L).courierName("DTDC")
				.status(DeliveryStatus.SHIPPED).build();

		assertThrows(NoBooksFoundException.class, () -> service.updatePrintIssueByMagazine(1L, req, authentication));
	}

	// ❌ Delivery not found
	@Test
	void updatePrintIssue_deliveryNotFound() {

		// 🔥 MUST
		when(authentication.getName()).thenReturn("admin@test.com");

		when(userRepo.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

		when(bookRepo.findByCategoryAndMagazineNo(any(), any())).thenReturn(Optional.of(book));

		when(deliveryRepo.findById(anyLong())).thenReturn(Optional.empty());

		UpdatePrintIssueRequest req = UpdatePrintIssueRequest.builder().magazineNo(2L).courierName("DTDC")
				.status(DeliveryStatus.SHIPPED).build();

		assertThrows(DeliveryNotFoundException.class,
				() -> service.updatePrintIssueByMagazine(1L, req, authentication));
	}

	// ✅ generateTodayShippedInvoices
	@Test
	void generateTodayShippedInvoices_success() {

		when(deliveryRepo.findByStatusAndDeliveryDate(DeliveryStatus.SHIPPED, LocalDate.now()))
				.thenReturn(List.of(delivery));

		when(invoiceService.generatePrintInvoice(anyList())).thenReturn(new byte[] { 1, 2, 3 });

		byte[] result = service.generateTodayShippedInvoices();

		assertNotNull(result);
		assertEquals(3, result.length);
	}

	// ❌ No shipped data
	@Test
	void generateTodayShippedInvoices_noData() {

		when(deliveryRepo.findByStatusAndDeliveryDate(DeliveryStatus.SHIPPED, LocalDate.now())).thenReturn(List.of());

		assertThrows(NoShippedDataFoundException.class, () -> service.generateTodayShippedInvoices());
	}
}
