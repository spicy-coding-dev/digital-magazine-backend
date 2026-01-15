package com.digital.magazine.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.digital.magazine.common.enums.Role;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.enums.AccountStatus;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	@Query("SELECT u FROM User u WHERE u.email = :login OR u.mobile = :login")
	Optional<User> findByEmailOrMobile(@Param("login") String login);

	Optional<User> findByEmail(String email);

	Optional<User> findByMobile(String mobile);

	boolean existsByEmail(String emailId);

	boolean existsByMobile(String userMobileNumber);

	boolean existsByRole(Role role);

	long countByStatus(AccountStatus status);

	Page<User> findByRole(Role role, Pageable pageable);

	List<User> findByStatusAndRole(AccountStatus status, Role role);

}
