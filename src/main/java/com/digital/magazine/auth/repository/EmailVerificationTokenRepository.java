package com.digital.magazine.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.digital.magazine.auth.entity.EmailVerificationToken;
import com.digital.magazine.user.entity.User;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

	Optional<EmailVerificationToken> findByToken(String token);

	void deleteByUser(User user);
}
