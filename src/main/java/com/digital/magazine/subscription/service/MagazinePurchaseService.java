package com.digital.magazine.subscription.service;

import org.springframework.security.core.Authentication;

public interface MagazinePurchaseService {

	public void purchase(Authentication auth, Long bookId);

}
