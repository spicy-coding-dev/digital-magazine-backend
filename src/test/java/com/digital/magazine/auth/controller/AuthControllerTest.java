package com.digital.magazine.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.digital.magazine.auth.dto.ForgotPasswordRequestDto;
import com.digital.magazine.auth.dto.LoginRequestDto;
import com.digital.magazine.auth.dto.RegisterRequestDto;
import com.digital.magazine.auth.service.AuthService;
import com.digital.magazine.common.enums.Role;
import com.digital.magazine.common.response.LoginApiResponse;
import com.digital.magazine.security.jwt.JwtAuthenticationFilter;
import com.digital.magazine.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

//
//@WebMvcTest(AuthController.class)
//@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

//	@MockBean
	private AuthService authService;

//	@MockBean
	private JwtUtil jwtUtil; // 🔥 FIX

//	@MockBean
	private JwtAuthenticationFilter jwtAuthenticationFilter; // 🔥 FIX

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void register_success() throws Exception {

		RegisterRequestDto dto = new RegisterRequestDto();
		dto.setName("Aslam");
		dto.setEmail("test@gmail.com");
		dto.setMobile("9876543210");
		dto.setPassword("Password@123");

		mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.message")
						.value("பதிவு வெற்றிகரமாக நிறைவடைந்தது. தயவுசெய்து உங்கள் மின்னஞ்சலை உறுதிப்படுத்தவும்."));
	}

	@Test
	void login_success() throws Exception {

		LoginRequestDto request = new LoginRequestDto();
		request.setEmailOrPhone("test@gmail.com");
		request.setPassword("Password@123");

		LoginApiResponse response = new LoginApiResponse("access-token", Role.USER);

		when(authService.login(any(), any())).thenReturn(response);

		mockMvc.perform(post("/api/v1/auth/user-login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.accessToken").exists());
	}

	@Test
	void forgotPassword_success() throws Exception {

		ForgotPasswordRequestDto dto = new ForgotPasswordRequestDto();
		dto.setEmailOrMobile("test@gmail.com");

		mockMvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto))).andExpect(status().isOk());
	}
}
