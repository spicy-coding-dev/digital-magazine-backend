package com.digital.magazine.common.storage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Service
public class SupabaseStorageService {

	private final WebClient webClient;

	@Value("${supabase.url}")
	private String supabaseUrl;

	@Value("${supabase.key}")
	private String supabaseKey;

	@Value("${supabase.bucket.public}")
	private String publicBucketName;

	@Value("${supabase.bucket.private}")
	private String privateBucketName;

	public SupabaseStorageService(WebClient.Builder builder) {

		HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
				.responseTimeout(Duration.ofSeconds(15));

		this.webClient = builder.clientConnector(new ReactorClientHttpConnector(httpClient))
				.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024)) // ⭐ 50MB
				.build();

	}

	public Mono<String> fetchFile(String url) {
		return webClient.get().uri(url).retrieve().bodyToMono(String.class).timeout(Duration.ofSeconds(20)) // extra
																											// global
																											// timeout
				.onErrorResume(ex -> {
					if (ex instanceof WebClientResponseException wex) {
						// HTTP status based error
						if (wex.getStatusCode() == HttpStatus.NOT_FOUND) {
							return Mono.just("File not found!");
						}
						return Mono.error(new RuntimeException("HTTP Error: " + wex.getStatusCode(), wex));
					} else if (ex instanceof ReadTimeoutException || ex instanceof TimeoutException) {
						// Timeout error
						return Mono.just("Request timed out! Please try again.");
					} else if (ex instanceof java.net.ConnectException) {
						// Connection refused / DNS error
						return Mono.just("Unable to connect to server!");
					}
					// Other error
					return Mono.error(new RuntimeException("Unexpected error occurred", ex));
				});
	}

	/* ===================== PUBLIC UPLOAD ===================== */

	public String uploadPublicFile(MultipartFile file, String folder) {
		log.info("📤 [PUBLIC UPLOAD START] file={} folder={}", file.getOriginalFilename(), folder);

		String url = upload(file, folder, publicBucketName, true);

		log.info("✅ [PUBLIC UPLOAD SUCCESS] url={}", url);
		return url;
	}

	/* ===================== PRIVATE UPLOAD ===================== */

//	public String uploadPrivateFile(MultipartFile file, String folder) {
//		log.info("🔐 [PRIVATE UPLOAD START] file={} folder={}", file.getOriginalFilename(), folder);
//
//		String path = upload(file, folder, privateBucketName, false);
//
//		log.info("✅ [PRIVATE UPLOAD SUCCESS] path={}", path);
//		return path;
//	}

	/* ===================== CORE UPLOAD ===================== */

	private String upload(MultipartFile file, String folder, String bucket, boolean isPublic) {

		try {
			String originalName = file.getOriginalFilename();

			String extension = (originalName != null && originalName.contains("."))
					? originalName.substring(originalName.lastIndexOf("."))
					: "";

			String uniqueFileName = UUID.randomUUID() + "_"
					+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + extension;

			String filePath = folder + "/" + uniqueFileName;

			String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + filePath;

			log.info("📤 Uploading to Supabase | bucket={} path={} method={}", bucket, filePath,
					isPublic ? "PUT" : "POST");

			WebClient.RequestBodySpec request = isPublic ? webClient.put().uri(uploadUrl)
					: webClient.post().uri(uploadUrl); // 🔥 KEY FIX

			request.header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey).header("x-upsert", "true")
					.header(HttpHeaders.CONTENT_TYPE,
							file.getContentType() != null ? file.getContentType() : "application/octet-stream")
					.bodyValue(new InputStreamResource(file.getInputStream())).retrieve().bodyToMono(String.class)
					.block();

			if (isPublic) {
				return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + filePath;
			}

			return filePath;

		} catch (Exception e) {
			log.error("❌ [UPLOAD FAILED] bucket={} file={}", bucket, file.getOriginalFilename(), e);
			throw new RuntimeException("Supabase upload failed", e);
		}
	}

	public void deletePublicFile(String publicUrl) {

		if (publicUrl == null || publicUrl.isBlank()) {
			log.warn("⚠️ [PUBLIC DELETE SKIPPED] Empty URL");
			return;
		}

		try {
			String relativePath = publicUrl
					.substring(publicUrl.indexOf("/object/public/") + "/object/public/".length());

			String deleteUrl = supabaseUrl + "/storage/v1/object/" + relativePath;

			log.info("🗑️ [PUBLIC DELETE START] path={}", relativePath);

			webClient.delete().uri(deleteUrl).header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey).retrieve()
					.bodyToMono(Void.class).block();

			log.info("✅ [PUBLIC DELETE SUCCESS] path={}", relativePath);

		} catch (Exception e) {
			log.error("❌ [PUBLIC DELETE FAILED] url={}", publicUrl, e);
		}
	}

//	public void deletePrivateFile(String filePath) {
//
//		if (filePath == null || filePath.isBlank()) {
//			log.warn("⚠️ [PRIVATE DELETE SKIPPED] Empty path");
//			return;
//		}
//
//		try {
//			String deleteUrl = supabaseUrl + "/storage/v1/object/" + privateBucketName + "/" + filePath;
//
//			log.info("🗑️ [PRIVATE DELETE START] bucket={} path={}", privateBucketName, filePath);
//
//			webClient.delete().uri(deleteUrl).header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey).retrieve()
//					.bodyToMono(Void.class).block();
//
//			log.info("✅ [PRIVATE DELETE SUCCESS] path={}", filePath);
//
//		} catch (Exception e) {
//			log.error("❌ [PRIVATE DELETE FAILED] bucket={} path={}", privateBucketName, filePath, e);
//		}
//	}

	/* ===================== SIGNED URL ===================== */

//	public String generateSignedUrlFromPrivate(String bucket, String filePath, int expirySeconds) {
//
//		log.info("🔐 [SIGNED URL REQUEST] bucket={} path={} expiry={}s", bucket, filePath, expirySeconds);
//
//		try {
//			String signUrl = supabaseUrl + "/storage/v1/object/sign/" + bucket + "/" + filePath;
//
//			Map<String, Object> body = Map.of("expiresIn", expirySeconds);
//
//			String response = webClient.post().uri(signUrl).header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey)
//					.bodyValue(body).retrieve().bodyToMono(String.class).block();
//
//			JsonNode json = new ObjectMapper().readTree(response);
//			String signedUrl = supabaseUrl + "/storage/v1" + json.get("signedURL").asText();
//
//			log.info("✅ [SIGNED URL GENERATED]");
//			return signedUrl;
//
//		} catch (Exception e) {
//			log.error("❌ [SIGNED URL FAILED] path={}", filePath, e);
//			throw new RuntimeException("Signed URL generation failed", e);
//		}
//	}
//
//	public InputStream getPrivatePdf(String filePath) {
//
//		String url = supabaseUrl + "/storage/v1/object/" + privateBucketName + "/" + filePath;
//
//		InputStreamResource resource = webClient.get().uri(url)
//				.header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey).retrieve()
//				.bodyToMono(InputStreamResource.class).block();
//
//		if (resource == null) {
//			throw new RuntimeException("Failed to fetch PDF from Supabase");
//		}
//
//		try {
//			return resource.getInputStream();
//		} catch (IOException e) {
//			throw new RuntimeException("Unable to read PDF stream", e);
//		}
//	}

}
