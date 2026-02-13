package com.digital.magazine.payment.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.common.response.ApiResponse;
import com.digital.magazine.payment.dto.CreateOrderRequestDto;
import com.digital.magazine.payment.dto.RazorpayOrderResponseDto;
import com.digital.magazine.payment.dto.RazorpayVerifyRequestDto;
import com.digital.magazine.payment.service.PaymentPreCheckService;
import com.digital.magazine.payment.service.RazorpayService;
import com.digital.magazine.subscription.dto.BuySubscriptionRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

	private final RazorpayService razorpayService;

	private final PaymentPreCheckService preCheckService;

	@Value("${razorpay.api.key}")
	private String razorpayKeyId;

	@PostMapping("/pre-check/single/{bookId}")
	public ResponseEntity<ApiResponse<String>> preCheckSingle(@PathVariable Long bookId, Authentication auth) {

		log.info("🔍 Pre-check single book | user={} | bookId={}", auth.getName(), bookId);

		preCheckService.preCheckSingleBook(auth, bookId);

		return ResponseEntity.ok(new ApiResponse<>("OK", "இந்த இதழை வாங்கலாம்"));
	}

	@PostMapping("/pre-check/subscription")
	public ResponseEntity<ApiResponse<String>> preCheckSubscription(@RequestBody BuySubscriptionRequest req,
			Authentication auth) {

		log.info("🔍 Pre-check subscription | user={} | planId={}", auth.getName(), req.getPlanId());

		preCheckService.preCheckSubscription(auth, req);

		return ResponseEntity.ok(new ApiResponse<>("OK", "இந்த சந்தாவை வாங்கலாம்"));
	}

	@PostMapping("/create-order")
	public ResponseEntity<ApiResponse<RazorpayOrderResponseDto>> createOrder(@RequestBody CreateOrderRequestDto req) {

		log.info("💰 Create payment | amount={} | purpose={}", req.getAmount(), req.getPurpose());

		RazorpayOrderResponseDto response = razorpayService.createOrder(req.getAmount(), req.getPurpose());

		return ResponseEntity.ok(new ApiResponse<>("Payment order created", response));
	}

	@PostMapping("/verify")
	public ResponseEntity<ApiResponse<String>> verifyPayment(@RequestBody RazorpayVerifyRequestDto req,
			Authentication auth) {

		log.info("🔍 Payment verification request | user={}", auth.getName());

		String message = razorpayService.verifyAndProcessPayment(req, auth);

		return ResponseEntity.ok(new ApiResponse<>("Payment successful", message));
	}

}
