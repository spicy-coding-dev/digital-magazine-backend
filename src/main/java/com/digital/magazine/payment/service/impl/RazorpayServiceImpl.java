package com.digital.magazine.payment.service.impl;

import java.time.LocalDateTime;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.digital.magazine.common.exception.PaymentInitiationException;
import com.digital.magazine.common.exception.PaymentVerificationFailedException;
import com.digital.magazine.common.exception.UserNotFoundException;
import com.digital.magazine.payment.dto.RazorpayOrderResponseDto;
import com.digital.magazine.payment.dto.RazorpayVerifyRequestDto;
import com.digital.magazine.payment.entity.PaymentTransaction;
import com.digital.magazine.payment.enums.PaymentStatus;
import com.digital.magazine.payment.repository.PaymentTransactionRepository;
import com.digital.magazine.payment.service.RazorpayService;
import com.digital.magazine.payment.util.RazorpayUtils;
import com.digital.magazine.subscription.dto.BuySubscriptionRequest;
import com.digital.magazine.subscription.service.MagazinePurchaseService;
import com.digital.magazine.subscription.service.SubscriptionService;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RazorpayServiceImpl implements RazorpayService {

	@Value("${razorpay.api.key}")
	private String keyId;

	@Value("${razorpay.secret.key}")
	private String keySecret;

	private final UserRepository userRepo;
	private final SubscriptionService subscriptionService;
	private final MagazinePurchaseService magazinePurchaseService;
	private final PaymentTransactionRepository paymentRepo;

	@Override
	public RazorpayOrderResponseDto createOrder(Double amount, String receipt) {

		try {
			RazorpayClient client = new RazorpayClient(keyId, keySecret);

			JSONObject options = new JSONObject();
			options.put("amount", amount * 100); // 🔥 paise
			options.put("currency", "INR");
			options.put("receipt", receipt);

			Order order = client.orders.create(options);

			String orderId = order.get("id").toString();
			Long orderAmount = ((Number) order.get("amount")).longValue();

			log.info("✅ Razorpay order created | orderId={}", orderId);

			return RazorpayOrderResponseDto.builder().orderId(orderId).amount(orderAmount).currency("INR").key(keyId)
					.build();

		} catch (Exception e) {
			log.error("❌ Razorpay order creation failed | amount={} | receipt={}", amount, receipt, e);
			throw new PaymentInitiationException("Payment initiation failed. Please try again.", e);
		}
	}

	@Override
	public String verifyAndProcessPayment(RazorpayVerifyRequestDto req, Authentication auth) {

		String payload = req.getRazorpayOrderId() + "|" + req.getRazorpayPaymentId();

		boolean isValid = RazorpayUtils.verifySignature(payload, req.getRazorpaySignature(), keySecret);

		if (!isValid) {
			log.error("❌ Payment verification failed | orderId={}", req.getRazorpayOrderId());
			throw new PaymentVerificationFailedException("Payment verification failed");
		}

		User user = userRepo.findByEmail(auth.getName()).orElseThrow(() -> new UserNotFoundException("User not found"));

		log.info("✅ Payment verified | paymentId={} | user={}", req.getRazorpayPaymentId(), user.getEmail());

		String resultMessage;

		switch (req.getPaymentType()) {

		case SUBSCRIPTION -> {
			BuySubscriptionRequest subReq = new BuySubscriptionRequest();
			subReq.setPlanId(req.getPlanId());
			subReq.setAddressId(req.getAddressId());

			resultMessage = subscriptionService.buy(subReq, auth);
		}

		case SINGLE_BOOK -> {
			resultMessage = magazinePurchaseService.purchase(auth, req.getBookId());
		}

		default -> throw new IllegalStateException("Invalid payment type");
		}

		// 💾 Save payment
		PaymentTransaction txn = PaymentTransaction.builder().user(user).paymentType(req.getPaymentType())
				.amount(req.getAmount()).paymentDate(LocalDateTime.now()).status(PaymentStatus.SUCCESS)
				.gatewayRef(req.getRazorpayPaymentId()).build();

		paymentRepo.save(txn);

		log.info("💾 Payment saved | txnId={}", txn.getId());

		return resultMessage;
	}

//	private BuySubscriptionRequest parseSubscriptionRequest(Map<String, String> data) {
//
//		BuySubscriptionRequest req = new BuySubscriptionRequest();
//		req.setPlanId(Long.valueOf(data.get("planId")));
//
//		if (data.containsKey("addressId")) {
//			req.setAddressId(Long.valueOf(data.get("addressId")));
//		}
//
//		return req;
//	}

}
