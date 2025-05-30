package com.ecommerce.productservice.service.export;

import static java.util.Objects.isNull;

import com.ecommerce.productservice.exception.UnAuthorizedException;
import com.ecommerce.productservice.payload.response.ProductResponseDTO;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProductPdfExporter extends AbstractExporter {

  private static final Logger LOGGER = LogManager.getLogger(ProductPdfExporter.class.getName());

  public void export(
      List<ProductResponseDTO> listCategories, HttpServletResponse response, String role)
      throws IOException {
    // Check if the user has the required role
    if (!isNull(role) && role.equals("ROLE_USER")) {
      LOGGER.error("*** {} ***", "Role needs to be ADMIN to export category details in Pdf");
      throw new UnAuthorizedException("Requires ROLE_ADMIN to download Pdf");
    }
    super.setResponseHeader(response, "application/pdf", ".pdf", "products_");
    Document document = new Document(PageSize.A3);
    PdfWriter.getInstance(document, response.getOutputStream());
    document.open();
    Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
    font.setSize(18);
    font.setColor(Color.BLUE);
    Paragraph paragraph = new Paragraph("List of Products", font);
    paragraph.setAlignment(Paragraph.ALIGN_CENTER);
    document.add(paragraph);
    PdfPTable table = new PdfPTable(8);
    table.setWidthPercentage(100f);
    table.setSpacingBefore(25);
    table.setWidths(new float[] {3.5f, 4.5f, 10.5f, 2.5f, 3.5f, 3.5f, 4.0f, 3.5f});
    writeTableHeader(table);
    writeTableData(table, listCategories);
    document.add(table);
    LOGGER.info("{}", "Pdf file downloaded successfully!");
    document.close();
  }

  private void writeTableData(PdfPTable table, List<ProductResponseDTO> listProducts) {

    for (ProductResponseDTO product : listProducts) {
      table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
      table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
      table.getDefaultCell().setPadding(5);
      table.addCell(String.valueOf(product.getProductId()));
      table.addCell(String.valueOf(product.getProductBrand()));
      table.addCell(String.valueOf(product.getProductName()));
      table.addCell(String.valueOf(product.getProductColor()));
      table.addCell(String.valueOf(product.isEnabled()));
      table.addCell(String.valueOf(product.getProductPrice()));
      table.addCell(String.valueOf(product.getDiscountPercent()));
      table.addCell(String.valueOf(product.getTotalPrice()));
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
    cell.setPhrase(new Phrase("Product ID", font));
    table.addCell(cell);
    cell.setPhrase(new Phrase("Product Brand", font));
    table.addCell(cell);
    cell.setPhrase(new Phrase("Product Name", font));
    table.addCell(cell);
    cell.setPhrase(new Phrase("Color", font));
    table.addCell(cell);
    cell.setPhrase(new Phrase("Enabled", font));
    table.addCell(cell);
    cell.setPhrase(new Phrase("Price", font));
    table.addCell(cell);
    cell.setPhrase(new Phrase("Discount %", font));
    table.addCell(cell);
    cell.setPhrase(new Phrase("Total Price", font));
    table.addCell(cell);
  }
}
