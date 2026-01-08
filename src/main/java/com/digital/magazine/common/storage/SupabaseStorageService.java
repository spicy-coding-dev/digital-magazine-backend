package com.digital.magazine.common.storage;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	@Value("${supabase.bucket}")
	private String bucketName;

	public SupabaseStorageService(WebClient.Builder builder) {

		HttpClient httpClient = HttpClient.create()
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
				.responseTimeout(Duration.ofSeconds(15));

		this.webClient = builder
				.clientConnector(new ReactorClientHttpConnector(httpClient))
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

	public String uploadFile(MultipartFile file, String folder) throws IOException {
		try {
			String originalFileName = file.getOriginalFilename();
			log.info("Uploading file: {}", originalFileName);

			String extension = (originalFileName != null && originalFileName.contains("."))
					? originalFileName.substring(originalFileName.lastIndexOf("."))
					: "";

			String uniqueFileName = UUID.randomUUID().toString() + "_"
					+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + extension;

			log.debug("Generated unique file name: {}", uniqueFileName);

			String filePath = folder + "/" + uniqueFileName;

			String url = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath;

			webClient.put().uri(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey)
					.header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
					.bodyValue(new InputStreamResource(file.getInputStream())).retrieve().bodyToMono(String.class)
					.block();

			log.info("File uploaded successfully: {}", filePath);

			return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + filePath;

		} catch (IOException e) {
			log.error("Error uploading file: {}", file.getOriginalFilename(), e);
			throw new RuntimeException("Error uploading file: " + file.getOriginalFilename(), e);
		}
	}

	public void deleteFileFromSupabase(String fileUrl) {

		if (fileUrl == null || fileUrl.isEmpty())
			return;

		// fileUrl la irundhu relative path extract pannanum
		// ex:
		// https://xyz.supabase.co/storage/v1/object/public/mybucket/userProfilePics/abc.jpg
		String relativePath = fileUrl.substring(fileUrl.indexOf(bucketName) + bucketName.length() + 1);

		String url = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + relativePath;

		webClient.delete().uri(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey).retrieve()
				.bodyToMono(Void.class).block();

		log.info("Deleting file from Supabase: {}", fileUrl);

	}

	// Generate signed URL for single file
	public String generateSignedUrl(String publicUrl, int expiryInSeconds) {

		log.info("Generating signed URL for file: {}", publicUrl);

		if (publicUrl == null || publicUrl.isEmpty())
			return "";
		try {
			String relativePath = publicUrl.replace(supabaseUrl + "/storage/v1/object/public/" + bucketName + "/", "");

			// encode path safely except "/"
			String safePath = URLEncoder.encode(relativePath, StandardCharsets.UTF_8.toString())
					.replaceAll("\\+", "%20").replaceAll("%2F", "/"); // keep slashes

			String url = supabaseUrl + "/storage/v1/object/sign/" + bucketName + "/" + safePath;

			Map<String, Object> requestBody = Map.of("expiresIn", expiryInSeconds);

			String signedUrlJson = webClient.post().uri(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey)
					.bodyValue(requestBody).retrieve().bodyToMono(String.class).block();

			JsonNode jsonNode = new ObjectMapper().readTree(signedUrlJson);
			return supabaseUrl + "/storage/v1" + jsonNode.get("signedURL").asText(); // Full https:// URL

		} catch (Exception e) {
			log.error("Error generating signed URL for {}: {}", publicUrl, e.getMessage(), e);
			throw new RuntimeException("Error generating signed URL " + e.getMessage(), e);
		}
	}

}
