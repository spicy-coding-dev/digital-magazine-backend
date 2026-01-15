package com.digital.magazine.analytics.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.digital.magazine.analytics.entity.UserSession;
import com.digital.magazine.user.entity.User;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

	Optional<UserSession> findTopByUserOrderByLoginTimeDesc(User user);

}
