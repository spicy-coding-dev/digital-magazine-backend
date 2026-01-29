package com.digital.magazine.subscription.service;

import org.springframework.security.core.Authentication;

public interface MagazinePurchaseService {

	public String purchase(Authentication auth, Long bookId);

}
