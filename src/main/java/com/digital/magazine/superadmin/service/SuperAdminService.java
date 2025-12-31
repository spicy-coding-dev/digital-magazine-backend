package com.digital.magazine.superadmin.service;

import com.digital.magazine.superadmin.dto.CreateAdminRequestDto;

public interface SuperAdminService {

	void createAdmin(CreateAdminRequestDto dto);

	public String verifyEmail(String token);
}
