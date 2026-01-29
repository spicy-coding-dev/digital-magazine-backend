package com.digital.magazine.book.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.digital.magazine.common.enums.BookCategory;
import com.digital.magazine.common.enums.BookStatus;
import com.digital.magazine.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Books {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title; // manual

	@Column(nullable = false)
	private String author; // admin / publisher

	@Column(nullable = false)
	private String subtitle;

	private Long magazineNo;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private BookCategory category;

	@Column(nullable = false)
	private String coverImagePath; // Supabase URL

	private boolean paid;
	private Double price;

	@Enumerated(EnumType.STRING)
	private BookStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "book_tags", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
	@Builder.Default
	private Set<Tag> tags = new HashSet<>();

	@Column(nullable = false)
	private String pdfPath; // PRIVATE bucket path ONLY

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
