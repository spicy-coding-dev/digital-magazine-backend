package com.digital.magazine.common.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SupabaseStorageServiceTest {

	@Mock
	private WebClient.Builder webClientBuilder;

	@Mock
	private WebClient webClient;

	@Mock
	private WebClient.RequestBodyUriSpec requestBodyUriSpec;

	@Mock
	private WebClient.RequestBodySpec requestBodySpec;

	@SuppressWarnings("rawtypes")
	@Mock
	private WebClient.RequestHeadersSpec requestHeadersSpec;

	@SuppressWarnings("rawtypes")
	@Mock
	private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

	@Mock
	private WebClient.ResponseSpec responseSpec;

	private SupabaseStorageService service;

	@BeforeEach
	void setup() {

		// builder chain fix
		when(webClientBuilder.clientConnector(any())).thenReturn(webClientBuilder);
		when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
		when(webClientBuilder.build()).thenReturn(webClient);

		service = new SupabaseStorageService(webClientBuilder);

		ReflectionTestUtils.setField(service, "supabaseUrl", "http://supabase.test");
		ReflectionTestUtils.setField(service, "supabaseKey", "test-key");
		ReflectionTestUtils.setField(service, "publicBucketName", "public");

		ReflectionTestUtils.setField(service, "webClient", webClient);

		// PUT (public upload)
		when(webClient.put()).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
		when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
		when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
		when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

		// DELETE (public delete)
		when(webClient.delete()).thenReturn(requestHeadersUriSpec);

		when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);

		when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersUriSpec);

		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
	}

	// ===================== uploadPublicFile =====================
	@Test
	void uploadPublicFile_success() throws Exception {

		MultipartFile file = mock(MultipartFile.class);
		when(file.getOriginalFilename()).thenReturn("test.pdf");
		when(file.getContentType()).thenReturn("application/pdf");
		when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

		when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("OK"));

		String url = service.uploadPublicFile(file, "books");

		assertNotNull(url);
		assertTrue(url.contains("/storage/v1/object/public/"));
	}

	// ===================== deletePublicFile =====================
	@Test
	void deletePublicFile_success() {

		when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

		String url = "http://supabase.test/storage/v1/object/public/public/books/test.pdf";

		assertDoesNotThrow(() -> service.deletePublicFile(url));

		verify(webClient).delete();
	}

	@Test
	void deletePublicFile_emptyUrl_shouldSkip() {

		assertDoesNotThrow(() -> service.deletePublicFile(""));

		verify(webClient, never()).delete();
	}

	// ===================== fetchFile =====================
	@Test
	void fetchFile_success() {

		when(webClient.get()).thenReturn(requestHeadersUriSpec);

		when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);

		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

		when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("FILE DATA"));

		String result = service.fetchFile("http://test/file").block();

		assertEquals("FILE DATA", result);
	}

}
