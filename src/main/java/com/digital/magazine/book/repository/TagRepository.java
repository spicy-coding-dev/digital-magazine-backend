package com.digital.magazine.book.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.digital.magazine.book.entity.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

	Optional<Tag> findByNameIgnoreCase(String name);

	Optional<Tag> findByName(String name);

}
