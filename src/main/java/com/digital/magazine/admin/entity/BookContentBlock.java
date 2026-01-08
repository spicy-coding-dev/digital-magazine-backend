package com.digital.magazine.admin.entity;

import com.digital.magazine.common.enums.ContentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "book_content_blocks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookContentBlock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id", nullable = false)
	private Books book;

	@Column(nullable = false)
	private Integer pageNumber;   // ✅ ADD THIS
	
	@Column(nullable = false)
	private Integer blockOrder; // 1,2,3,4...

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ContentType type; // TEXT / IMAGE

	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String textContent; // only if TEXT

	private String imageUrl; // Supabase URL (only if IMAGE)
}
