package com.digital.magazine.subscription.service;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.user.entity.User;

public interface AccessService {

	public boolean canAccessBook(User user, Books book);

}
