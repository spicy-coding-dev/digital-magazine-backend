package com.digital.magazine.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.digital.magazine.user.entity.User;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String token;

	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;

	private LocalDateTime expiryTime;
}
