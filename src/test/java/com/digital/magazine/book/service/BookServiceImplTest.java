package com.digital.magazine.book.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import com.digital.magazine.book.dto.BookUploadRequestDto;
import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.entity.Tag;
import com.digital.magazine.book.repository.BookContentRepository;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.book.repository.TagRepository;
import com.digital.magazine.book.service.impl.BookServiceImpl;
import com.digital.magazine.common.enums.BookStatus;
import com.digital.magazine.common.exception.UserNotFoundException;
import com.digital.magazine.common.storage.SupabaseStorageService;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

	@InjectMocks
	private BookServiceImpl bookService;

	@Mock
	private BookRepository bookRepo;

	@Mock
	private UserRepository userRepository;

	@Mock
	private TagRepository tagRepo;

	@Mock
	private SupabaseStorageService supabaseStorageService;

	@Mock
	private BookContentRepository bookContentRepo;

	@Mock
	private UserDetails userDetails;

	@Mock
	private MultipartFile coverImage;

	private User admin;

	@BeforeEach
	void setup() {
		admin = User.builder().id(1L).email("admin@test.com").name("Admin").createdAt(LocalDateTime.now()).build();
	}

	// ✅ SUCCESS CASE
	@Test
	void uploadBook_success() {

		// given
		when(userDetails.getUsername()).thenReturn("admin@test.com");
		when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

		when(supabaseStorageService.uploadPublicFile(any(), eq("books/covers")))
				.thenReturn("https://supabase/book.jpg");

		when(tagRepo.findByNameIgnoreCase(any())).thenReturn(Optional.of(Tag.builder().name("history").build()));

		BookUploadRequestDto dto = BookUploadRequestDto.builder().title("Tamil History").subtitle("Chola Period")
				.author("Author A").category("வரலாறு").tags(List.of("History")).paid(false).status(BookStatus.DRAFT)
				.magazineNo(1L).build();

		// when
		bookService.uploadBook(dto, coverImage, userDetails);

		// then
		verify(bookRepo, times(1)).save(any(Books.class));
		verify(supabaseStorageService).uploadPublicFile(any(), eq("books/covers"));
	}

	// ❌ ADMIN NOT FOUND
	@Test
	void uploadBook_adminNotFound() {

		when(userDetails.getUsername()).thenReturn("admin@test.com");
		when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.empty());

		BookUploadRequestDto dto = BookUploadRequestDto.builder().title("Test Book").category("வரலாறு").build();

		assertThrows(UserNotFoundException.class, () -> bookService.uploadBook(dto, coverImage, userDetails));

		verify(bookRepo, never()).save(any());
	}

	// ✅ GET CONTENT SUCCESS
	@Test
	void getContentByBookId_success() {

		when(bookContentRepo.findByBookId(1L)).thenReturn(Optional
				.of(com.digital.magazine.book.entity.BookContent.builder().bookId(1L).content("<p>Hello</p>").build()));

		String content = bookService.getContentByBookId(1L);

		assertEquals("<p>Hello</p>", content);
	}

}
