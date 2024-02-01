package com.ecommerce.categoryservice.service.export;

import com.ecommerce.categoryservice.exception.UnAuthorizedException;
import com.ecommerce.categoryservice.payload.response.CategoryResponseDTO;
import com.lowagie.text.Font;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.util.List;

import static java.util.Objects.isNull;

public class CategoryPdfExporter extends AbstractExporter {

	private static final Logger LOGGER = LogManager.getLogger(CategoryPdfExporter.class.getName());

	public void export(List<CategoryResponseDTO> listCategories, HttpServletResponse response, String role)
			throws IOException {

		// Check if the user has the required role
		if (!isNull(role) && role.equals("ROLE_USER")) {
			LOGGER.error("*** {} ***", "Role needs to be ADMIN to export category details in Pdf");
			throw new UnAuthorizedException("Requires ROLE_ADMIN to download Pdf");
		}
		super.setResponseHeader(response, "application/pdf", ".pdf", "categories_");
		Document document = new Document(PageSize.A4);
		PdfWriter.getInstance(document, response.getOutputStream());
		document.open();
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setSize(18);
		font.setColor(Color.BLUE);
		Paragraph paragraph = new Paragraph("List of Categories", font);
		paragraph.setAlignment(Paragraph.ALIGN_CENTER);
		document.add(paragraph);
		PdfPTable table = new PdfPTable(4);
		table.setWidthPercentage(100f);
		table.setSpacingBefore(25);
		table.setWidths(new float[] { 6.5f, 12.5f, 4.5f, 4.5f });
		writeTableHeader(table);
		writeTableData(table, listCategories);
		document.add(table);
		LOGGER.info("{}", "Pdf file downloaded successfully!");
		document.close();
	}

	private void writeTableData(PdfPTable table, List<CategoryResponseDTO> listCategories) {

		for (CategoryResponseDTO category : listCategories) {
			table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
			table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
			table.getDefaultCell().setPadding(5);
			table.addCell(String.valueOf(category.getCategoryId()));
			table.addCell(category.getCategoryName());
			table.addCell(String.valueOf(category.isEnabled()));
			table.addCell(String.valueOf(category.isHasChildren()));
		}
	}

	private void writeTableHeader(PdfPTable table) {

		PdfPCell cell = new PdfPCell();
		cell.setBackgroundColor(Color.BLUE);
		cell.setPadding(5);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setColor(Color.WHITE);
		cell.setPhrase(new Phrase("Category ID", font));
		table.addCell(cell);
		cell.setPhrase(new Phrase("Category Name", font));
		table.addCell(cell);
		cell.setPhrase(new Phrase("Enabled", font));
		table.addCell(cell);
		cell.setPhrase(new Phrase("Parent", font));
		table.addCell(cell);
	}

}
