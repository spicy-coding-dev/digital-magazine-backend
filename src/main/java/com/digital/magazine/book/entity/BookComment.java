package com.digital.magazine.book.entity;

import java.time.LocalDateTime;

import com.digital.magazine.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "book_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookComment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 🔗 Book
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id", nullable = false)
	private Books book;

	// 👤 User
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	// 💬 User comment
	@Column(nullable = false, length = 2000)
	private String content;

	// 🛠 Admin reply
	@Column(length = 2000)
	private String adminReply;

	private boolean adminReplied;
	private LocalDateTime adminRepliedAt;

	// system flags
	private boolean edited;
	private boolean deleted;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@PrePersist
	void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = this.createdAt;
		this.adminReplied = false; // 🔥 IMPORTANT
		this.deleted = false;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now();
		this.edited = true;
	}
}
