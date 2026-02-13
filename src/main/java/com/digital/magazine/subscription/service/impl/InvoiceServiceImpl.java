package com.digital.magazine.subscription.service.impl;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.digital.magazine.subscription.entity.PrintDelivery;
import com.digital.magazine.subscription.service.InvoiceService;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

	/**
	 * 1 A4 = 4 Stickers Works for ANY count (1,5,10...)
	 */
	@Override
	public byte[] generatePrintInvoice(List<PrintDelivery> deliveries) {

		if (deliveries == null || deliveries.isEmpty()) {
			log.warn("⚠️ Invoice generation skipped | No deliveries provided");
			return new byte[0];
		}

		log.info("📄 Invoice generation started | totalDeliveries={}", deliveries.size());

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Document document = new Document(PageSize.A4, 20, 20, 20, 20);
			PdfWriter writer = PdfWriter.getInstance(document, baos);
			document.open();

			log.debug("📑 PDF document opened (A4)");

			Font companyFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
			Font labelFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
			Font textFont = new Font(Font.FontFamily.HELVETICA, 8);

			// ---- Sticker layout (A4 : 2 x 2) ----
			float stickerWidth = 260;
			float stickerHeight = 300;

			float leftMargin = 30;
			float topStart = 820;
			float gap = 20;

			int index = 0;

			for (PrintDelivery d : deliveries) {

				// New page after every 4 stickers
				if (index > 0 && index % 4 == 0) {
					document.newPage();
					log.debug("📄 New PDF page created | stickerIndex={}", index);
				}

				int pos = index % 4;

				float x = (pos % 2 == 0) ? leftMargin : leftMargin + stickerWidth + gap;

				float y = (pos < 2) ? topStart : topStart - stickerHeight - 40;

				log.debug("🧾 Rendering sticker | index={} | user={} | magazineNo={}", index,
						d.getSubscription().getUser().getName(), d.getBook().getMagazineNo());

				PdfContentByte cb = writer.getDirectContent();

				// 🔲 Sticker Border (Courier style)
				cb.rectangle(x, y - stickerHeight, stickerWidth, stickerHeight);
				cb.stroke();

				ColumnText ct = new ColumnText(cb);

				// 🔥 Content safe area (padding inside border)
				ct.setSimpleColumn(x + 8, y - stickerHeight + 8, x + stickerWidth - 8, y - 8);

				// ------------------ CONTENT ------------------

				Paragraph company = new Paragraph("DIGITAL MAGAZINE", companyFont);
				company.setAlignment(Element.ALIGN_CENTER);
				ct.addElement(company);

				Paragraph addr = new Paragraph("No.12, Anna Nagar\nMadurai - 625001\nPh: 9876543210", textFont);
				addr.setAlignment(Element.ALIGN_CENTER);
				addr.setSpacingAfter(6);
				ct.addElement(addr);

				ct.addElement(divider());

				// TO ADDRESS (Courier format)
				ct.addElement(new Paragraph("TO", labelFont));
				ct.addElement(new Paragraph(d.getSubscription().getUser().getName(), textFont));
				ct.addElement(new Paragraph(d.getSubscription().getDeliveryAddress().getAddressLine(), textFont));
				ct.addElement(new Paragraph(d.getSubscription().getDeliveryAddress().getCity() + " - "
						+ d.getSubscription().getDeliveryAddress().getPincode(), textFont));
				ct.addElement(
						new Paragraph("Mobile : " + d.getSubscription().getDeliveryAddress().getMobile(), textFont));

				ct.addElement(divider());

				// Magazine details
				ct.addElement(new Paragraph("PARTICULARS", labelFont));
				ct.addElement(new Paragraph("Title : " + d.getBook().getTitle(), textFont));
				ct.addElement(new Paragraph("Magazine No : " + d.getBook().getMagazineNo(), textFont));

				ct.addElement(divider());

				// Footer
				ct.addElement(
						new Paragraph("Courier : " + d.getCourierName() + " | Status : " + d.getStatus(), textFont));

				ct.addElement(new Paragraph("Issued By : " + d.getIssuedBy() + " | Date : "
						+ d.getDeliveryDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), textFont));

				ct.go();
				index++;
			}

			document.close();

			log.info("✅ Invoice generation completed | totalStickers={} | pages={}", index, (index / 4) + 1);
			return baos.toByteArray();

		} catch (Exception e) {
			log.error("❌ Invoice generation failed", e);
			return null;
		}
	}

	// -----------------------------------------------------

	private Paragraph divider() {
		Paragraph p = new Paragraph("--------------------------------");
		p.setSpacingBefore(4);
		p.setSpacingAfter(4);
		return p;
	}

}
