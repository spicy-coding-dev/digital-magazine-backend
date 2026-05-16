package com.digital.magazine.payment.util;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RazorpayUtils {

	public static boolean verifySignature(String payload, String actualSignature, String secret) {

		try {

			log.info("🔐 Razorpay signature verification started");

			Mac sha256Hmac = Mac.getInstance("HmacSHA256");

			SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

			sha256Hmac.init(secretKey);

			byte[] hash = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

			String generatedSignature = bytesToHex(hash);

			log.info("🔥 GENERATED SIGNATURE = {}", generatedSignature);

			log.info("🔥 ACTUAL SIGNATURE = {}", actualSignature);

			boolean matched = generatedSignature.equals(actualSignature);

			if (matched) {
				log.info("✅ Razorpay signature VERIFIED");
			} else {
				log.warn("❌ Razorpay signature MISMATCH");
			}

			return matched;

		} catch (Exception e) {

			log.error("❌ Signature verification error", e);

			return false;
		}
	}

	private static String bytesToHex(byte[] bytes) {

		StringBuilder sb = new StringBuilder();

		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}

		return sb.toString();
	}
}