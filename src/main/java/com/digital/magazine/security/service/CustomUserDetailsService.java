package com.digital.magazine.security.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepo;

	@Override
	public UserDetails loadUserByUsername(String usernameOrEmailOrPhone) throws UsernameNotFoundException {

		User user = userRepo.findByEmailOrMobile(usernameOrEmailOrPhone)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		return new org.springframework.security.core.userdetails.User(user.getEmail(), // username
				user.getPassword(), // BCrypt password
				Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
	}

}
