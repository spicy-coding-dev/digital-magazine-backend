package com.digital.magazine.payment.util;

import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RazorpayUtils {

	private static final String HMAC_SHA256 = "HmacSHA256";

	public static boolean verifySignature(String payload, String actualSignature, String secret) {

		log.info("🔐 Razorpay signature verification started");

		try {
			String generatedSignature = hmacSha256(payload, secret);

			boolean match = generatedSignature.equals(actualSignature);

			if (match) {
				log.info("✅ Razorpay signature VERIFIED successfully");
			} else {
				log.warn("❌ Razorpay signature MISMATCH");
			}

			return match;

		} catch (Exception e) {
			log.error("🔥 Razorpay signature verification ERROR", e);

			// ❌ util la exception throw panna vendam
			// service decide pannum
			return false;
		}
	}

	private static String hmacSha256(String data, String secret) throws Exception {

		log.debug("🔑 Generating HMAC-SHA256 signature");

		SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA256);

		Mac mac = Mac.getInstance(HMAC_SHA256);
		mac.init(secretKey);

		byte[] rawHmac = mac.doFinal(data.getBytes());

		return Base64.getEncoder().encodeToString(rawHmac);
	}
}
