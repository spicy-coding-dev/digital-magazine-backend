package com.digital.magazine.admin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.digital.magazine.admin.dto.AdminUserDto;
import com.digital.magazine.admin.service.impl.AdminUserServiceImpl;
import com.digital.magazine.common.enums.Role;
import com.digital.magazine.common.exception.InvalidUserRoleException;
import com.digital.magazine.common.exception.UserPendingException;
import com.digital.magazine.common.service.EmailService;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.enums.AccountStatus;
import com.digital.magazine.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

	@Mock
	private UserRepository userRepo;

	@Mock
	private EmailService emailService;

	@InjectMocks
	private AdminUserServiceImpl service;

	private User activeUser;
	private User blockedUser;
	private User pendingUser;
	private User adminUser;

	@BeforeEach
	void setup() {

		activeUser = User.builder().id(1L).name("Active User").email("active@test.com").role(Role.USER)
				.status(AccountStatus.ACTIVE).emailVerified(true).createdAt(LocalDateTime.now()).build();

		blockedUser = User.builder().id(2L).name("Blocked User").email("blocked@test.com").role(Role.USER)
				.status(AccountStatus.BLOCKED).build();

		pendingUser = User.builder().id(3L).name("Pending User").email("pending@test.com").role(Role.USER)
				.status(AccountStatus.PENDING).build();

		adminUser = User.builder().id(10L).name("Admin").email("admin@test.com").role(Role.ADMIN)
				.status(AccountStatus.ACTIVE).build();
	}

	// --------------------------------------------------
	// ✅ getAllUsers
	// --------------------------------------------------
	@Test
	void getAllUsers_success() {

		Pageable pageable = PageRequest.of(0, 10);
		Page<User> page = new PageImpl<>(List.of(activeUser));

		when(userRepo.findByRole(Role.USER, pageable)).thenReturn(page);

		Page<AdminUserDto> result = service.getAllUsers(pageable);

		assertEquals(1, result.getTotalElements());
		assertEquals("active@test.com", result.getContent().get(0).getEmail());
		verify(userRepo).findByRole(Role.USER, pageable);
	}

	// --------------------------------------------------
	// ✅ toggleUserBlock : ACTIVE → BLOCKED
	// --------------------------------------------------
	@Test
	void toggleUserBlock_activeUser_shouldBlock() {

		when(userRepo.findById(1L)).thenReturn(java.util.Optional.of(activeUser));

		service.toggleUserBlock(1L, "Violation");

		assertEquals(AccountStatus.BLOCKED, activeUser.getStatus());
		verify(emailService).sendUserBlockedMail(eq("active@test.com"), eq("Violation"));
		verify(userRepo).save(activeUser);
	}

	// --------------------------------------------------
	// ✅ toggleUserBlock : BLOCKED → ACTIVE
	// --------------------------------------------------
	@Test
	void toggleUserBlock_blockedUser_shouldUnblock() {

		when(userRepo.findById(2L)).thenReturn(java.util.Optional.of(blockedUser));

		service.toggleUserBlock(2L, "Cleared");

		assertEquals(AccountStatus.ACTIVE, blockedUser.getStatus());
		verify(emailService).sendUserUnblockedMail(eq("blocked@test.com"), eq("Cleared"));
		verify(userRepo).save(blockedUser);
	}

	// --------------------------------------------------
	// ❌ toggleUserBlock : PENDING user
	// --------------------------------------------------
	@Test
	void toggleUserBlock_pendingUser_shouldThrowException() {

		when(userRepo.findById(3L)).thenReturn(java.util.Optional.of(pendingUser));

		assertThrows(UserPendingException.class, () -> service.toggleUserBlock(3L, "Reason"));

		verify(userRepo, never()).save(any());
	}

	// --------------------------------------------------
	// ❌ toggleUserBlock : NON-USER role
	// --------------------------------------------------
	@Test
	void toggleUserBlock_adminUser_shouldThrowException() {

		when(userRepo.findById(10L)).thenReturn(java.util.Optional.of(adminUser));

		assertThrows(InvalidUserRoleException.class, () -> service.toggleUserBlock(10L, "Reason"));

		verify(userRepo, never()).save(any());
	}

	// --------------------------------------------------
	// ✅ sendBulkMailByStatus : NORMAL MAIL
	// --------------------------------------------------
	@Test
	void sendBulkMailByStatus_normalMail_success() {

		when(userRepo.findByStatusAndRole(AccountStatus.ACTIVE, Role.USER)).thenReturn(List.of(activeUser));

		service.sendBulkMailByStatus(AccountStatus.ACTIVE, "Subject", "Content", null, null);

		verify(emailService).sendEmail(eq("active@test.com"), eq("Subject"), eq("Content"));
	}

	// --------------------------------------------------
	// ✅ sendBulkMailByStatus : ATTACHMENT MAIL
	// --------------------------------------------------
	@Test
	void sendBulkMailByStatus_attachmentMail_success() {

		byte[] attachment = "PDF".getBytes();

		when(userRepo.findByStatusAndRole(AccountStatus.ACTIVE, Role.USER)).thenReturn(List.of(activeUser));

		service.sendBulkMailByStatus(AccountStatus.ACTIVE, "Subject", "Content", attachment, "file.pdf");

		verify(emailService).sendMailWithAttachment(eq("active@test.com"), eq("Subject"), eq("Content"), eq(attachment),
				eq("file.pdf"));
	}

	// --------------------------------------------------
	// ⚠️ sendBulkMailByStatus : NO USERS
	// --------------------------------------------------
	@Test
	void sendBulkMailByStatus_noUsers_shouldSkip() {

		when(userRepo.findByStatusAndRole(AccountStatus.BLOCKED, Role.USER)).thenReturn(List.of());

		service.sendBulkMailByStatus(AccountStatus.BLOCKED, "Subject", "Content", null, null);

		verify(emailService, never()).sendEmail(any(), any(), any());
	}
}
