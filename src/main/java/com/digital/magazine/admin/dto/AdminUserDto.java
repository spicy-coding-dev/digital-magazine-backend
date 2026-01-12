package com.digital.magazine.admin.dto;

import java.time.LocalDateTime;

import com.digital.magazine.common.enums.Role;
import com.digital.magazine.user.enums.AccountStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserDto {

    private Long id;
    private String name;
    private String email;
    private String mobile;
    private Role role;
    private AccountStatus status;
    private boolean emailVerified;
    private LocalDateTime createdAt;
}

