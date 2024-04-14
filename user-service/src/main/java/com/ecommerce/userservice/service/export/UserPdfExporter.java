package com.ecommerce.userservice.service.export;

import static java.util.Objects.isNull;

import com.ecommerce.userservice.exception.UnAuthorizedException;
import com.ecommerce.userservice.payload.response.UserDTOResponse;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserPdfExporter extends AbstractExporter {

  private static final Logger LOGGER = LogManager.getLogger(UserPdfExporter.class.getName());

  public void export(List<UserDTOResponse> listUsers, HttpServletResponse response, String role)
      throws IOException {
    // Check if the user has the required role
    if (!isNull(role) && role.equals("ROLE_USER")) {
      LOGGER.error("*** {} ***", "Role needs to be ADMIN to export user details in Pdf");
      throw new UnAuthorizedException("Requires ROLE_ADMIN to download Pdf");
    }
    super.setResponseHeader(response, "application/pdf", ".pdf", "users_");
    Document document = new Document(PageSize.A3);
    PdfWriter.getInstance(document, response.getOutputStream());
    response.flushBuffer();
    document.open();
    Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
    font.setSize(18);
    font.setColor(Color.BLUE);
    Paragraph paragraph = new Paragraph("List of Users", font);
    paragraph.setAlignment(Paragraph.ALIGN_CENTER);
    document.add(paragraph);
    PdfPTable table = new PdfPTable(6);
    table.setWidthPercentage(100f);
    table.setSpacingBefore(20);
    table.setWidths(new float[] {4.0f, 2.5f, 2.5f, 1.5f, 1.0f, 3.5f});
    writeTableHeader(table);
    writeTableData(table, listUsers);
    document.add(table);
    LOGGER.info("{}", "Pdf file downloaded successfully!");
    document.close();
  }

  private void writeTableData(PdfPTable table, List<UserDTOResponse> listUsers) {

    for (UserDTOResponse user : listUsers) {
      table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
      table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
      table.getDefaultCell().setPadding(5);
      table.addCell(String.valueOf(user.getEmailId()));
      table.addCell(user.getFirstName());
      table.addCell(user.getLastName());
      table.addCell(String.valueOf(user.isEnabled()));
      table.addCell(String.valueOf(user.getAge()));
      table.addCell(user.getRole().name());
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
    cell.setPhrase(new Phrase("Email", font));
    table.addCell(cell);
    cell.setPhrase(new Phrase("First Name", font));
    table.addCell(cell);
    cell.setPhrase(new Phrase("Last Name", font));
    table.addCell(cell);
    cell.setPhrase(new Phrase("Enabled", font));
    table.addCell(cell);
    cell.setPhrase(new Phrase("Age", font));
    table.addCell(cell);
    cell.setPhrase(new Phrase("Role", font));
    table.addCell(cell);
  }
}
