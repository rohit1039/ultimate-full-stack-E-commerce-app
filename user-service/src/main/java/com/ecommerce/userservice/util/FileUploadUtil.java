package com.ecommerce.userservice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileUploadUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadUtil.class);

	public static void saveFile(String fileName, MultipartFile multipartFile) throws IOException {

		Path uploadPath = Paths.get("user-images");
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}
		try (InputStream inputStream = multipartFile.getInputStream()) {
			Path filePath = uploadPath.resolve(fileName);
			Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
			LOGGER.info("Image copied to destination folder successfully!");
		}
		catch (IOException ioe) {
			LOGGER.error("Unable to save image file! ");
			throw new IOException("Could not save file: " + fileName, ioe);
		}
	}

}
