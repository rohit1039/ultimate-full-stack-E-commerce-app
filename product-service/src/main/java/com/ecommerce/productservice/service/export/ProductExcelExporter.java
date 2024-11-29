package com.ecommerce.productservice.service.export;

import static java.util.Objects.isNull;

import com.ecommerce.productservice.exception.UnAuthorizedException;
import com.ecommerce.productservice.payload.response.ProductResponseDTO;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ProductExcelExporter extends AbstractExporter {

  private static final Logger LOGGER = LogManager.getLogger(ProductExcelExporter.class);

  private final XSSFWorkbook workbook;

  private XSSFSheet sheet;

  public ProductExcelExporter() {

    workbook = new XSSFWorkbook();
  }

  private void writeHeaderLine() {

    sheet = workbook.createSheet("Products");
    XSSFRow row = sheet.createRow(0);
    XSSFCellStyle cellStyle = workbook.createCellStyle();
    cellStyle.setAlignment(HorizontalAlignment.CENTER);
    XSSFFont font = workbook.createFont();
    font.setBold(true);
    font.setFontHeight(16);
    cellStyle.setFont(font);
    createCell(row, 0, "Category ID", cellStyle);
    createCell(row, 1, "Product ID", cellStyle);
    createCell(row, 2, "Product Brand", cellStyle);
    createCell(row, 3, "Product Name", cellStyle);
    createCell(row, 4, "Product Color", cellStyle);
    createCell(row, 5, "Enabled", cellStyle);
    createCell(row, 6, "Price", cellStyle);
    createCell(row, 7, "Discount %", cellStyle);
    createCell(row, 8, "Total Price", cellStyle);
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
    if (value instanceof Long) {
      cell.setCellValue((Long) value);
    } else if (value instanceof Boolean) {
      cell.setCellValue((Boolean) value);
    } else if (value instanceof Float) {
      cell.setCellValue((Float) value);
    } else {
      cell.setCellValue((String) value);
    }
    cell.setCellStyle(style);
  }

  /**
   * @param listProducts
   * @param response
   * @throws IOException
   */
  public void export(
      List<ProductResponseDTO> listProducts, HttpServletResponse response, String role)
      throws IOException {
    // Check if the user has the required role
    if (!isNull(role) && role.equals("ROLE_USER")) {
      LOGGER.error("*** {} ***", "Role needs to be ADMIN to export product details in Excel");
      throw new UnAuthorizedException("Requires ROLE_ADMIN to download Excel");
    }
    super.setResponseHeader(response, "application/octet-stream", ".xlsx", "products_");
    writeHeaderLine();
    writeDataLines(listProducts);
    LOGGER.info("{}", "Excel file downloaded successfully!");
    ServletOutputStream outputStream = response.getOutputStream();
    workbook.write(outputStream);
    workbook.close();
    outputStream.close();
  }

  /**
   * @param listProducts
   */
  private void writeDataLines(List<ProductResponseDTO> listProducts) {

    int rowIndex = 1;
    XSSFCellStyle cellStyle = workbook.createCellStyle();
    cellStyle.setAlignment(HorizontalAlignment.CENTER);
    XSSFFont font = workbook.createFont();
    font.setFontHeight(14);
    cellStyle.setFont(font);
    for (ProductResponseDTO product : listProducts) {
      XSSFRow row = sheet.createRow(rowIndex++);
      int columnIndex = 0;
      createCell(row, columnIndex++, product.getCategoryId(), cellStyle);
      createCell(row, columnIndex++, product.getProductId(), cellStyle);
      createCell(row, columnIndex++, product.getProductBrand(), cellStyle);
      createCell(row, columnIndex++, product.getProductName(), cellStyle);
      createCell(row, columnIndex++, product.getProductColor(), cellStyle);
      createCell(row, columnIndex++, product.isEnabled(), cellStyle);
      createCell(row, columnIndex++, product.getProductPrice(), cellStyle);
      createCell(row, columnIndex++, product.getDiscountPercent(), cellStyle);
      createCell(row, columnIndex++, product.getTotalPrice(), cellStyle);
    }
  }
}
