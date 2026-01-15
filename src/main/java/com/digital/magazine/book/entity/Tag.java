package com.digital.magazine.book.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tags", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50, unique = true)
	private String name; // history, tamil, politics
}
