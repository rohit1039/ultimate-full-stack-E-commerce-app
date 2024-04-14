package com.ecommerce.productservice.util;

import static java.util.Objects.isNull;

import com.ecommerce.productservice.exception.UnAuthorizedException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class FileUploadUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadUtil.class);

  public static void saveFile(
      Integer productId, String fileName, MultipartFile multipartFile, String role)
      throws IOException {

    if (!isNull(role) && role.equals("ROLE_USER")) {
      LOGGER.error("*** {} ***", "Role needs to be ADMIN to update category");
      throw new UnAuthorizedException("Requires ROLE_ADMIN to update a category");
    }
    Path uploadPath = Paths.get("product-images" + "/" + productId);
    if (!Files.exists(uploadPath)) {
      Files.createDirectories(uploadPath);
    }
    try (InputStream inputStream = multipartFile.getInputStream()) {
      Path filePath = uploadPath.resolve(fileName);
      Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
      LOGGER.info("Image:{} uploaded successfully!", fileName);
    } catch (IOException ioe) {
      LOGGER.error("Unable to save image file! ");
      throw new IOException("Could not save file: " + fileName, ioe);
    }
  }

  public static void saveMultiFiles(Integer productId, MultipartFile[] multipartFiles, String role)
      throws IOException {

    if (!isNull(role) && role.equals("ROLE_USER")) {
      LOGGER.error("*** {} ***", "Role needs to be ADMIN to update category");
      throw new UnAuthorizedException("Requires ROLE_ADMIN to update a category");
    }
    Path uploadPath = Paths.get("product-images" + "/" + "extras" + "/" + productId);
    if (!Files.exists(uploadPath)) {
      Files.createDirectories(uploadPath);
    }
    for (MultipartFile multipartFile : multipartFiles) {
      try (InputStream inputStream = multipartFile.getInputStream()) {
        Path filePath =
            uploadPath.resolve(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        LOGGER.info("Image:{} uploaded successfully!", multipartFile.getOriginalFilename());
      } catch (IOException ioe) {
        LOGGER.error("Unable to save image file! ");
        throw new IOException("Could not save file: " + multipartFile.getOriginalFilename(), ioe);
      }
    }
  }
}
