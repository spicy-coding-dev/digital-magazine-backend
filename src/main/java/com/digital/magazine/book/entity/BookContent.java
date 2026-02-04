package com.digital.magazine.book.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookContent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "book_id", nullable = false, unique = true)
	private Long bookId;

	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String content;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
