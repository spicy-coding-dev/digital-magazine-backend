package com.digital.magazine.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.digital.magazine.common.enums.Role;
import com.digital.magazine.security.service.CustomUserDetailsService;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

	@Mock
	private UserRepository userRepo;

	@InjectMocks
	private CustomUserDetailsService service;

	@Test
	void loadUserByUsername_success() {

		User user = User.builder().email("user@test.com").password("encoded-password").role(Role.USER).build();

		when(userRepo.findByEmailOrMobile("user@test.com")).thenReturn(Optional.of(user));

		UserDetails userDetails = service.loadUserByUsername("user@test.com");

		assertNotNull(userDetails);
		assertEquals("user@test.com", userDetails.getUsername());
		assertEquals("encoded-password", userDetails.getPassword());

		assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
	}

	@Test
	void loadUserByUsername_userNotFound_shouldThrowException() {

		when(userRepo.findByEmailOrMobile("unknown@test.com")).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("unknown@test.com"));
	}
}
