package com.digital.magazine.payment.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.common.response.ApiResponse;
import com.digital.magazine.payment.dto.CreateOrderRequestDto;
import com.digital.magazine.payment.dto.RazorpayOrderResponseDto;
import com.digital.magazine.payment.dto.RazorpayVerifyRequestDto;
import com.digital.magazine.payment.service.RazorpayService;
import com.razorpay.Order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

	private final RazorpayService razorpayService;

	@Value("${razorpay.api.key}")
	private String razorpayKeyId;

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
