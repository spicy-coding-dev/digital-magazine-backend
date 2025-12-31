package com.digital.magazine.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.digital.magazine.common.enums.Role;
import com.digital.magazine.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	@Query("SELECT u FROM User u WHERE u.email = :login OR u.mobile = :login")
	Optional<User> findByEmailOrMobile(@Param("login") String login);

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String emailId);

	boolean existsByMobile(String userMobileNumber);

	boolean existsByRole(Role role);

}
