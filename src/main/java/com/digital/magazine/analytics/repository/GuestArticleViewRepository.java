package com.digital.magazine.analytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.digital.magazine.analytics.entity.GuestArticleView;

public interface GuestArticleViewRepository extends JpaRepository<GuestArticleView, Long> {
	
	void deleteByArticleId(Long articleId);

}
