package com.digital.magazine.admin.repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.digital.magazine.admin.entity.Books;

@Repository
public interface BookRepository extends JpaRepository<Books, Long> {
	
	Optional<Books> findById(Long Id);
	
	@Query("""
			   SELECT b FROM Books b
			   WHERE b.category = :category
			     AND b.status = 'PUBLISHED'
			   ORDER BY b.createdAt DESC
			""")
			List<Books> findLatestByCategory(
			    @Param("category") String category,
			    Pageable pageable
			);


}
