//package com.digital.magazine.auth.repository;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import com.digital.magazine.auth.entity.PasswordResetToken;
//import com.digital.magazine.user.entity.User;
//import com.digital.magazine.user.enums.AccountStatus;
//import com.digital.magazine.common.enums.Role;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.domain.EntityScan;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.test.context.ActiveProfiles;
//
//@DataJpaTest
//@ActiveProfiles("test")
//@EntityScan(basePackages = "com.digital.magazine")
//@EnableJpaRepositories(basePackages = "com.digital.magazine")
//class PasswordResetTokenRepositoryTest {
//
//	@Autowired
//	private PasswordResetTokenRepository tokenRepository;
//
//	@Autowired
//	private TestEntityManager entityManager;
//
//	@Test
//	void save_and_findByToken_success() {
//
//		User user = User.builder().name("Aslam").email("test@gmail.com").mobile("9876543210").password("password")
//				.role(Role.USER).status(AccountStatus.ACTIVE).emailVerified(true).createdAt(LocalDateTime.now())
//				.build();
//
//		entityManager.persist(user);
//
//		PasswordResetToken token = new PasswordResetToken();
//		token.setToken("reset-token-123");
//		token.setExpiryTime(LocalDateTime.now().plusHours(1));
//		token.setUsed(false);
//		token.setUser(user);
//
//		entityManager.persist(token);
//		entityManager.flush();
//
//		Optional<PasswordResetToken> result = tokenRepository.findByToken("reset-token-123");
//
//		assertThat(result).isPresent();
//	}
////
////	@Test
////	void findByToken_notFound() {
////
////		Optional<PasswordResetToken> result = tokenRepository.findByToken("invalid-token");
////
////		assertThat(result).isEmpty();
////	}
//}
