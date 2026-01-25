package com.digital.magazine.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.digital.magazine.admin.dto.AdminUserDto;

public interface AdminUserService {

	Page<AdminUserDto> getAllUsers(Pageable pageable);

	void toggleUserBlock(Long userId, String reason);

//	@Async("taskExecutor")
//	public void sendBulkMailByStatus(AccountStatus status, String subject, String content, byte[] attachmentBytes,
//			String fileName);

}
