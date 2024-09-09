package com.ecommerce.userservice.service.export;

import static java.util.Objects.isNull;

import com.ecommerce.userservice.exception.MissingHeaderException;
import com.ecommerce.userservice.payload.response.UserDTOResponse;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.*;

public class UserExcelExporter extends AbstractExporter {

  private static final Logger LOGGER = LogManager.getLogger(UserExcelExporter.class.getName());

  private final XSSFWorkbook workbook;

  private XSSFSheet sheet;

  public UserExcelExporter() {

    workbook = new XSSFWorkbook();
  }

  private void writeHeaderLine() {

    sheet = workbook.createSheet("Users");
    XSSFRow row = sheet.createRow(0);
    XSSFCellStyle cellStyle = workbook.createCellStyle();
    cellStyle.setAlignment(HorizontalAlignment.CENTER);
    XSSFFont font = workbook.createFont();
    font.setBold(true);
    font.setFontHeight(16);
    cellStyle.setFont(font);
    createCell(row, 0, "Email", cellStyle);
    createCell(row, 1, "First Name", cellStyle);
    createCell(row, 2, "Last Name", cellStyle);
    createCell(row, 3, "Enabled", cellStyle);
    createCell(row, 4, "Age", cellStyle);
    createCell(row, 5, "Role", cellStyle);
  }

  /**
   * @param row
   * @param columnIndex
   * @param value
   * @param style
   */
  private void createCell(XSSFRow row, int columnIndex, Object value, XSSFCellStyle style) {

    XSSFCell cell = row.createCell(columnIndex);
    sheet.autoSizeColumn(columnIndex);
    if (value instanceof Integer) {
      cell.setCellValue((Integer) value);
    } else if (value instanceof Boolean) {
      cell.setCellValue((Boolean) value);
    } else {
      cell.setCellValue((String) value);
    }
    cell.setCellStyle(style);
  }

  /**
   * @param listUsers
   * @param response
   * @throws IOException
   */
  public void export(List<UserDTOResponse> listUsers, HttpServletResponse response, String role)
      throws IOException {
    // Check if the user has the required role
    if (!isNull(role) && role.equals("ROLE_USER")) {
      LOGGER.error("*** {} ***", "Role needs to be ADMIN to export user details in Excel");
      throw new MissingHeaderException("Requires ROLE_ADMIN to download Excel");
    }
    super.setResponseHeader(response, "application/octet-stream", ".xlsx", "users_");
    writeHeaderLine();
    writeDataLines(listUsers);
    LOGGER.info("{}", "Excel file downloaded successfully!");
    ServletOutputStream outputStream = response.getOutputStream();
    workbook.write(outputStream);
    workbook.close();
    outputStream.close();
  }

  /**
   * @param listUsers
   */
  private void writeDataLines(List<UserDTOResponse> listUsers) {

    int rowIndex = 1;
    XSSFCellStyle cellStyle = workbook.createCellStyle();
    cellStyle.setAlignment(HorizontalAlignment.CENTER);
    XSSFFont font = workbook.createFont();
    font.setFontHeight(14);
    cellStyle.setFont(font);
    for (UserDTOResponse user : listUsers) {
      XSSFRow row = sheet.createRow(rowIndex++);
      int columnIndex = 0;
      createCell(row, columnIndex++, user.getEmailId(), cellStyle);
      createCell(row, columnIndex++, user.getFirstName(), cellStyle);
      createCell(row, columnIndex++, user.getLastName(), cellStyle);
      createCell(row, columnIndex++, user.isEnabled(), cellStyle);
      createCell(row, columnIndex++, user.getAge(), cellStyle);
      createCell(row, columnIndex++, user.getRole().name(), cellStyle);
    }
  }
}
