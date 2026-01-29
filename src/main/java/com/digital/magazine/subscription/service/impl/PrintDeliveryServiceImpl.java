package com.digital.magazine.subscription.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.common.enums.BookCategory;
import com.digital.magazine.common.exception.DeliveryNotFoundException;
import com.digital.magazine.common.exception.NoBooksFoundException;
import com.digital.magazine.common.exception.NoShippedDataFoundException;
import com.digital.magazine.common.exception.UserNotFoundException;
import com.digital.magazine.subscription.dto.PrintDeliveryResponseDto;
import com.digital.magazine.subscription.dto.UpdatePrintIssueRequest;
import com.digital.magazine.subscription.entity.PrintDelivery;
import com.digital.magazine.subscription.enums.DeliveryStatus;
import com.digital.magazine.subscription.repository.PrintDeliveryRepository;
import com.digital.magazine.subscription.service.InvoiceService;
import com.digital.magazine.subscription.service.PrintDeliveryService;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrintDeliveryServiceImpl implements PrintDeliveryService {

	private final UserRepository userRepo;
	private final BookRepository bookRepo;
	private final PrintDeliveryRepository deliveryRepo;
	private final InvoiceService invoiceService;
	private static final BookCategory PRINT_CATEGORY = BookCategory.fromTamil("இதழ்கள்");

	@Override
	public List<PrintDeliveryResponseDto> getPrintIssues(Long id) {

		log.info("📥 Fetch print issues | subscriptionId={}", id);

		List<PrintDelivery> deliveries = deliveryRepo.findBySubscriptionId(id);

		log.info("📦 Print issues count={}", deliveries.size());

		return deliveries.stream().map(this::mapToDto).toList();
	}

	@Override
	public void updatePrintIssueByMagazine(Long deliveryId, UpdatePrintIssueRequest req, Authentication auth) {

		User currentUser = userRepo.findByEmail(auth.getName())
				.orElseThrow(() -> new UserNotFoundException("பயனர் காணப்படவில்லை"));

		Books book = bookRepo.findByCategoryAndMagazineNo(PRINT_CATEGORY, req.getMagazineNo())
				.orElseThrow(() -> new NoBooksFoundException("இந்த Print இதழ் கிடைக்கவில்லை"));

		PrintDelivery delivery = deliveryRepo.findById(deliveryId).orElseThrow(
				() -> new DeliveryNotFoundException("Delivery details not found (விநியோக விவரம் கிடைக்கவில்லை)"));

		if (req.getDeliveryDate() != null) {
			delivery.setDeliveryDate(req.getDeliveryDate());
		}

		delivery.setBook(book);
		delivery.setCourierName(req.getCourierName());
		delivery.setIssuedBy(currentUser.getName());
		delivery.setStatus(req.getStatus());

		deliveryRepo.save(delivery);

		log.info("✅ Print issue updated | deliveryId={} | status={}", deliveryId, req.getStatus());
	}

	@Override
	public byte[] generateTodayShippedInvoices() {

		LocalDate today = LocalDate.now();

		List<PrintDelivery> deliveries = deliveryRepo.findByStatusAndDeliveryDate(DeliveryStatus.SHIPPED, today);

		if (deliveries.isEmpty()) {
			log.warn("⚠️ No SHIPPED deliveries found for {}", today);

			throw new NoShippedDataFoundException("இன்றைய shipped data இல்லை");
		}

		return invoiceService.generatePrintInvoice(deliveries);
	}

	private PrintDeliveryResponseDto mapToDto(PrintDelivery d) {

		var sub = d.getSubscription();
		var user = sub.getUser();
		var addr = sub.getDeliveryAddress(); // 🔥 PRINT subscription address

		return PrintDeliveryResponseDto.builder().deliveryId(d.getId()).subscriptionId(sub.getId()).userId(user.getId())
				.userName(user.getName()).userEmail(user.getEmail())

				.addressName(addr.getName()).addressLine(addr.getAddressLine()).city(addr.getCity())
				.state(addr.getState()).pincode(addr.getPincode()).mobile(addr.getMobile())

				.bookTitle(d.getBook() != null ? d.getBook().getTitle() : null)
				.magazineNo(d.getBook() != null ? d.getBook().getMagazineNo() : null)

				.deliveryDate(d.getDeliveryDate()).courierName(d.getCourierName()).issuedBy(d.getIssuedBy())
				.status(d.getStatus()).build();
	}
}
