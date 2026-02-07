package com.digital.magazine.payment.service;

import org.springframework.security.core.Authentication;

import com.digital.magazine.payment.dto.RazorpayOrderResponseDto;
import com.digital.magazine.payment.dto.RazorpayVerifyRequestDto;

public interface RazorpayService {

	public RazorpayOrderResponseDto createOrder(Double amount, String receipt);

	public String verifyAndProcessPayment(RazorpayVerifyRequestDto req, Authentication auth);

}
