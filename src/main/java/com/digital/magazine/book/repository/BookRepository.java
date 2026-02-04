package com.digital.magazine.book.repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.digital.magazine.book.entity.Books;
import com.digital.magazine.common.enums.BookCategory;
import com.digital.magazine.common.enums.BookStatus;

@Repository
public interface BookRepository extends JpaRepository<Books, Long> {

	Optional<Books> findById(Long Id);

	@Query("""
				SELECT b FROM Books b
				WHERE b.category = :category
				AND b.status = 'PUBLISHED'
				ORDER BY b.createdAt DESC
			""")
	List<Books> findLatestByCategory(@Param("category") BookCategory category, Pageable pageable);

	@Query("""
				SELECT b FROM Books b
				WHERE (:category IS NULL OR b.category = :category)
				AND b.status = 'PUBLISHED'
				ORDER BY b.createdAt DESC
			""")
	List<Books> findForHome(@Param("category") BookCategory category, Pageable pageable);

	long countByStatus(BookStatus status);

	@Query("""
				SELECT COUNT(b)
				FROM Books b
				WHERE MONTH(b.createdAt) = MONTH(CURRENT_DATE)
				  AND YEAR(b.createdAt) = YEAR(CURRENT_DATE)
			""")
	long countBooksUploadedThisMonth();

	List<Books> findByCategoryAndStatus(BookCategory category, BookStatus status);

	Optional<Books> findByCategoryAndMagazineNo(BookCategory category, Long magazineNo);

	List<Books> findTop5ByCategoryAndStatusAndIdNotOrderByUpdatedAtDesc(BookCategory category, BookStatus status,
			Long id);

}
