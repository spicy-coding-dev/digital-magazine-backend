package com.digital.magazine.admin.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.digital.magazine.admin.dto.AdminUserDto;
import com.digital.magazine.admin.service.AdminUserService;
import com.digital.magazine.common.enums.Role;
import com.digital.magazine.common.exception.InvalidUserRoleException;
import com.digital.magazine.common.exception.UserPendingException;
import com.digital.magazine.common.service.EmailService;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.enums.AccountStatus;
import com.digital.magazine.user.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

	private final UserRepository userRepo;
	private final EmailService emailService;

	@Override
	public Page<AdminUserDto> getAllUsers(Pageable pageable) {

		log.info("📋 Fetching ONLY USER role accounts | page={}, size={}", pageable.getPageNumber(),
				pageable.getPageSize());

		Page<User> users = userRepo.findByRole(Role.USER, pageable);

		log.info("✅ USER accounts fetched | count={}", users.getTotalElements());

		return users.map(this::toDto);
	}

	@Override
	public void toggleUserBlock(Long userId, String reason) {

		log.info("🔄 Block toggle request received for userId={}", userId);

		User user = userRepo.findById(userId).orElseThrow(() -> {
			log.error("❌ User not found | userId={}", userId);
			return new RuntimeException("User not found");
		});

		// 🛑 Safety check
		if (user.getRole() != Role.USER) {
			log.warn("🚫 Attempt to block non-USER account | userId={}, role={}", userId, user.getRole());
			throw new InvalidUserRoleException("Only USER accounts can be blocked/unblocked");
		}

		// 🔁 Toggle logic
		if (user.getStatus() == AccountStatus.ACTIVE) {

			user.setStatus(AccountStatus.BLOCKED);
			emailService.sendUserBlockedMail(user.getEmail(), reason);
			log.info("⛔ User BLOCKED | userId={}, email={}", userId, user.getEmail());

		} else if (user.getStatus() == AccountStatus.BLOCKED) {

			user.setStatus(AccountStatus.ACTIVE);
			emailService.sendUserUnblockedMail(user.getEmail(), reason);
			log.info("✅ User UNBLOCKED | userId={}, email={}", userId, user.getEmail());

		} else if (user.getStatus() == AccountStatus.PENDING) {

			log.warn("✅ User PENDING | userId={}, email={}", userId, user.getEmail());
			throw new UserPendingException("Already this user pending user");

		} else {
			log.warn("⚠️ Block toggle ignored | userId={}, status={}", userId, user.getStatus());
		}

		userRepo.save(user);
		log.info("💾 User status updated successfully | userId={}, newStatus={}", userId, user.getStatus());
	}

//	@Override
//	@Async("taskExecutor")
//	public void sendBulkMailByStatus(AccountStatus status, String subject, String content, byte[] attachmentBytes,
//			String fileName) {
//
//		log.info("🔍 Fetching users with status={}", status);
//
//		List<User> users = userRepo.findByStatusAndRole(status, Role.USER);
//
//		if (users.isEmpty()) {
//			log.warn("⚠️ No users found with status={}", status);
//			return;
//		}
//
//		log.info("👥 Total {} users found for status={}", users.size(), status);
//
//		for (User user : users) {
//			try {
//
//				if (attachmentBytes != null) {
//					log.debug("📎 Sending mail WITH attachment to {}", user.getEmail());
//
//					emailService.sendMailWithAttachment(user.getEmail(), subject, content, attachmentBytes, fileName);
//				} else {
//					log.debug("📨 Sending normal mail to {}", user.getEmail());
//					emailService.sendEmail(user.getEmail(), subject, content);
//				}
//
//				log.info("✅ Mail sent | email={}, status={}", user.getEmail(), status);
//
//			} catch (Exception e) {
//				log.error("❌ Failed to send mail | email={}, reason={}", user.getEmail(), e.getMessage(), e);
//			}
//		}
//
//		log.info("📬 Bulk email process completed for status={}", status);
//	}

	private AdminUserDto toDto(User user) {

		return AdminUserDto.builder().id(user.getId()).name(user.getName()).email(user.getEmail())
				.mobile(user.getMobile()).role(user.getRole()).status(user.getStatus())
				.emailVerified(user.isEmailVerified()).createdAt(user.getCreatedAt()).build();
	}
}
