package com.digital.magazine.analytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.digital.magazine.analytics.entity.ArticleView;

public interface ArticleViewRepository extends JpaRepository<ArticleView, Long> {

	void deleteByArticleId(Long articleId);

}
