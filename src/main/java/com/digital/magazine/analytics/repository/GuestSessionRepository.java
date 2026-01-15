package com.digital.magazine.analytics.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.digital.magazine.analytics.entity.GuestSession;

public interface GuestSessionRepository extends JpaRepository<GuestSession, Long> {

	Optional<GuestSession> findByGuestId(String guestId);

}
