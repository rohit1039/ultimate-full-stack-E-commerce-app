package com.ecommerce.categoryservice.service.export;

import com.ecommerce.categoryservice.exception.UnAuthorizedException;
import com.ecommerce.categoryservice.payload.response.CategoryResponseDTO;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.*;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.isNull;

public class CategoryExcelExporter extends AbstractExporter {

	private static final Logger LOGGER = LogManager.getLogger(CategoryExcelExporter.class.getName());

	private final XSSFWorkbook workbook;

	private XSSFSheet sheet;

	public CategoryExcelExporter() {

		workbook = new XSSFWorkbook();
	}

	private void writeHeaderLine() {

		sheet = workbook.createSheet("Categories");
		XSSFRow row = sheet.createRow(0);
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		XSSFFont font = workbook.createFont();
		font.setBold(true);
		font.setFontHeight(16);
		cellStyle.setFont(font);
		createCell(row, 0, "Category ID", cellStyle);
		createCell(row, 1, "Category Name", cellStyle);
		createCell(row, 2, "Enabled", cellStyle);
		createCell(row, 3, "Parent", cellStyle);
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
		}
		else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
		}
		else {
			cell.setCellValue((String) value);
		}
		cell.setCellStyle(style);
	}

	/**
	 * @param listCategories
	 * @param response
	 * @throws IOException
	 */
	public void export(List<CategoryResponseDTO> listCategories, HttpServletResponse response, String role)
			throws IOException {

		// Check if the user has the required role
		if (!isNull(role) && role.equals("ROLE_USER")) {
			LOGGER.error("*** {} ***", "Role needs to be ADMIN to export category details in Excel");
			throw new UnAuthorizedException("Requires ROLE_ADMIN to download Excel");
		}
		super.setResponseHeader(response, "application/octet-stream", ".xlsx", "categories_");
		writeHeaderLine();
		writeDataLines(listCategories);
		LOGGER.info("{}", "Excel file downloaded successfully!");
		ServletOutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		workbook.close();
		outputStream.close();
	}

	/**
	 * @param listCategories
	 */
	private void writeDataLines(List<CategoryResponseDTO> listCategories) {

		int rowIndex = 1;
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		XSSFFont font = workbook.createFont();
		font.setFontHeight(14);
		cellStyle.setFont(font);
		for (CategoryResponseDTO category : listCategories) {
			XSSFRow row = sheet.createRow(rowIndex++);
			int columnIndex = 0;
			createCell(row, columnIndex++, category.getCategoryId(), cellStyle);
			createCell(row, columnIndex++, category.getCategoryName(), cellStyle);
			createCell(row, columnIndex++, category.isEnabled(), cellStyle);
			createCell(row, columnIndex++, category.isHasChildren(), cellStyle);
		}
	}

}
