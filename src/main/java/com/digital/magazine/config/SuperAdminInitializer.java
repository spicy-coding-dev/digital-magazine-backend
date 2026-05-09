package com.digital.magazine.config;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.digital.magazine.common.enums.Role;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.enums.AccountStatus;
import com.digital.magazine.user.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdminInitializer {

	private final UserRepository userRepo;
	private final PasswordEncoder passwordEncoder;

	@PostConstruct
	public void initSuperAdmin() {

		log.info("🔍 Checking Super Admin existence...");

		if (!userRepo.existsByRole(Role.SUPER_ADMIN)) {

			User superAdmin = User.builder().name("Super Admin").email("enquiry.spicycode@gmail.com")
					.mobile("8438365490").password(passwordEncoder.encode("Spicy@2025_8438")).role(Role.SUPER_ADMIN)
					.status(AccountStatus.ACTIVE).emailVerified(true).createdAt(LocalDateTime.now()).build();

			userRepo.save(superAdmin);

			log.info("👑 Default Super Admin created successfully");
		} else {
			log.info("✅ Super Admin already exists");
		}
	}

	@PostConstruct
	public void initAdmin() {

		log.info("🔍 Checking Admin existence...");

		if (!userRepo.existsByRole(Role.ADMIN)) {

			User admin = User.builder().name("Maanudam").email("mohamedaslam6369@gmail.com").mobile("8438365491")
					.password(passwordEncoder.encode("Spicy@2025")).role(Role.ADMIN).status(AccountStatus.ACTIVE)
					.emailVerified(true).createdAt(LocalDateTime.now()).build();

			userRepo.save(admin);

			log.info("👑 Default Admin created successfully");
		} else {
			log.info("✅ Admin already exists");
		}
	}

}
