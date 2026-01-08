package com.digital.magazine.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.digital.magazine.admin.entity.BookContentBlock;

@Repository
public interface BookContentBlockRepository extends JpaRepository<BookContentBlock, Long> {

	List<BookContentBlock> findByBookIdOrderByBlockOrder(Long bookId);

	List<BookContentBlock> findByBookIdOrderByPageNumberAscBlockOrderAsc(Long bookId);

}
