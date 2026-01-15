package com.digital.magazine.common.pdf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.web.multipart.MultipartFile;

import com.digital.magazine.book.entity.BookContentBlock;
import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.repository.BookContentBlockRepository;
import com.digital.magazine.common.enums.ContentType;
import com.digital.magazine.common.file.SimpleMultipartFile;
import com.digital.magazine.common.storage.SupabaseStorageService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PdfPageImageExtractor {

	private final Books book;
	private final BookContentBlockRepository blockRepo;
	private final SupabaseStorageService fileService;

	private int blockOrder = 1;

	public PdfPageImageExtractor(Books book, BookContentBlockRepository blockRepo, SupabaseStorageService fileService) {

		this.book = book;
		this.blockRepo = blockRepo;
		this.fileService = fileService;
	}

	public void extract(MultipartFile pdfFile) {

		log.info("📘 Starting PDF page-image extraction | bookId={} | file={}", book.getId(),
				pdfFile.getOriginalFilename());

		try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {

			PDFRenderer renderer = new PDFRenderer(document);
			int pages = document.getNumberOfPages();

			log.info("📄 Total pages detected: {}", pages);

			for (int page = 0; page < pages; page++) {

				int pageNo = page + 1;
				log.info("➡️ Processing page {}/{}", pageNo, pages);

				// 1️⃣ Render page image
				BufferedImage pageImage = renderer.renderImageWithDPI(page, 150);
				log.debug("🖼️ Page {} rendered as image", pageNo);

				// 2️⃣ Convert to MultipartFile
				MultipartFile imageFile = toMultipart(pageImage);

				// 3️⃣ Upload to Supabase
				String imageUrl = fileService.uploadFile(imageFile, "books/pages");
				log.info("☁️ Page {} uploaded | url={}", pageNo, imageUrl);

				// 4️⃣ Save DB record
				blockRepo.save(BookContentBlock.builder().book(book).pageNumber(pageNo).blockOrder(blockOrder++)
						.type(ContentType.IMAGE).imageUrl(imageUrl).build());

				log.info("✅ Page {} stored successfully", pageNo);
			}

			log.info("🎉 PDF extraction completed successfully | bookId={}", book.getId());

		} catch (Exception e) {
			log.error("❌ PDF extraction failed | bookId={}", book.getId(), e);
			throw new RuntimeException("PDF page image extraction failed", e);
		}
	}

	private MultipartFile toMultipart(BufferedImage image) throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);

		return new SimpleMultipartFile(baos.toByteArray(), "page-" + UUID.randomUUID() + ".png", "image/png");
	}
}
