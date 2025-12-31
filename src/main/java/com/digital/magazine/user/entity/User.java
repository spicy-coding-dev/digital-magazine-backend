package com.digital.magazine.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.digital.magazine.common.enums.Role;
import com.digital.magazine.user.enums.AccountStatus;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false, unique = true)
	private String mobile;

	@Column(nullable = false)
	private String password;

	private String country;
	private String state;
	private String district;

	@Enumerated(EnumType.STRING)
	private Role role; // USER, ADMIN, SUPER_ADMIN

	private boolean emailVerified;

	@Enumerated(EnumType.STRING)
	private AccountStatus status; // PENDING, ACTIVE, BLOCKED

	private LocalDateTime createdAt;
}
