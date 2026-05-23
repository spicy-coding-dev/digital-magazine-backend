package com.digital.magazine.subscription.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.digital.magazine.common.enums.Role;
import com.digital.magazine.subscription.entity.UserSubscription;
import com.digital.magazine.subscription.enums.SubscriptionStatus;
import com.digital.magazine.subscription.enums.SubscriptionType;
import com.digital.magazine.user.entity.User;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

	boolean existsByUserAndPlan_TypeAndStatus(User user, SubscriptionType type, SubscriptionStatus status);

	Optional<UserSubscription> findActiveByUser(User user);

	Optional<UserSubscription> findByUserAndStatus(User user, SubscriptionStatus status);

	// ✅ ACTIVE paid users (ROLE_USER only)
	@Query("""
			    SELECT COUNT(us)
			    FROM UserSubscription us
			    WHERE us.status = :status
			      AND us.user.role = :role
			""")
	long countPaidUsers(@Param("status") SubscriptionStatus status, @Param("role") Role role);

	List<UserSubscription> findByStatus(SubscriptionStatus status);

	List<UserSubscription> findByPlan_TypeAndStatus(SubscriptionType type, SubscriptionStatus status);

	@Query("""
				SELECT DISTINCT u.email
				FROM UserSubscription us
				JOIN us.user u
				WHERE us.plan.type = :type
				AND us.status = 'ACTIVE'
			""")
	List<String> findEmailBySubscriptionType(SubscriptionType type);

	boolean existsByUser_IdAndPlan_TypeAndStatus(Long userId, SubscriptionType type, SubscriptionStatus status);

	boolean existsByUserAndPlan_TypeAndStatusAndEndDateAfter(User user, SubscriptionType type,
			SubscriptionStatus status, LocalDate date);

	List<UserSubscription> findByStatusAndEndDate(SubscriptionStatus status, LocalDate endDate);

	List<UserSubscription> findByStatusInAndEndDateBefore(List<SubscriptionStatus> statuses, LocalDate today);

	Long countByStatus(SubscriptionStatus status);
}
