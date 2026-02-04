package com.digital.magazine.book.repository;

import java.util.Optional;

import org.springdoc.core.converters.models.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.digital.magazine.book.entity.BookComment;

@Repository
public interface BookCommentRepository extends JpaRepository<BookComment, Long> {

//	Page<BookComment> findByBookIdAndDeletedFalse(Long bookId, Pageable pageable);
//
//	Page<BookComment> findByAdminRepliedFalseAndDeletedFalse(Pageable pageable);
//
//	Optional<BookComment> findByIdAndDeletedFalse(Long id);
}
