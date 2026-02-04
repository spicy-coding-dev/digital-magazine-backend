package com.digital.magazine.book.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.digital.magazine.book.entity.BookContent;

public interface BookContentRepository extends JpaRepository<BookContent, Long> {

	Optional<BookContent> findByBookId(Long bookId);

	void deleteByBookId(Long bookId);
}
