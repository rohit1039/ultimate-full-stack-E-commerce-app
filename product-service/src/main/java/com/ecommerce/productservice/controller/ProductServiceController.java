package com.ecommerce.productservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.ecommerce.productservice.exception.ProductNotFoundException;
import com.ecommerce.productservice.payload.request.ProductRequestDTO;
import com.ecommerce.productservice.payload.response.ProductResponseDTO;
import com.ecommerce.productservice.service.ProductService;
import com.ecommerce.productservice.service.export.ProductExcelExporter;
import com.ecommerce.productservice.service.export.ProductPdfExporter;
import com.ecommerce.productservice.util.FileDownloadUtil;
import com.ecommerce.productservice.util.FileUploadUtil;
import com.ecommerce.productservice.util.ProductModelAssembler;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Tag(
    name = "Product Service",
    description =
        "Clients should use this service to get product's details only, "
            + "and"
            + " Admins should use this service to add, get, update and delete product details")
@Validated
public class ProductServiceController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceController.class);

  private final ProductService productService;

  private final ProductModelAssembler productAssembler;

  private final ModelMapper modelMapper;

  /**
   * Adds a new product to the database.
   *
   * @param productRequestDTO the product information to add
   * @param categoryId the ID of the category to which the product should be added
   * @return a response indicating whether the product was added successfully
   * @throws Exception if there was an error adding the product
   */
  @Operation(
      summary = "Add a new product",
      description = "A POST request to add products, accessible by <b> ADMINS" + " </b> only",
      tags = {"Product Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Successfully created the product"),
        @ApiResponse(responseCode = "400", description = "Input validation failed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user"),
        @ApiResponse(responseCode = "404", description = "Category Not" + " Found"),
        @ApiResponse(responseCode = "500", description = "Some error occurred"),
        @ApiResponse(responseCode = "409", description = "Product already exists")
      })
  @PostMapping(value = "/v1/add/{categoryId}")
  public ResponseEntity<EntityModel<ProductResponseDTO>> addProduct(
      @Schema(hidden = true) @RequestHeader(name = "loggedInUser") String username,
      @Schema(hidden = true) @RequestHeader(name = "userRole") String role,
      @Valid @RequestBody ProductRequestDTO productRequestDTO,
      @Parameter(in = ParameterIn.PATH, description = "id of the category to save the products")
          @PathVariable
          Integer categoryId)
      throws Exception {

    ProductResponseDTO productResponseDTO =
        this.productService.saveProductToDB(productRequestDTO, categoryId, username, role);
    EntityModel<ProductResponseDTO> response = this.productAssembler.toModel(productResponseDTO);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  /**
   * Returns a product with the specified ID.
   *
   * @param productId the ID of the product to retrieve
   * @return the product with the specified ID, or an error response if the product could not be
   *     found
   */
  @Operation(
      summary = "Get product by Id",
      description =
          "A GET request to get product by Id, accessible by <b> " + "CLIENTS & ADMINS </b>",
      tags = {"Product " + "Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully found the product"),
        @ApiResponse(responseCode = "400", description = "Input validation failed"),
        @ApiResponse(responseCode = "404", description = "Product Not Found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized" + " user"),
        @ApiResponse(responseCode = "500", description = "Some error " + "occurred")
      })
  @GetMapping("/v1/get/{productId}")
  public ResponseEntity<EntityModel<ProductResponseDTO>> getProductById(
      @Parameter(in = ParameterIn.PATH, description = "id of the product") @PathVariable
          Integer productId) {

    ProductResponseDTO productResponseDTO = this.productService.getProductById(productId);
    EntityModel<ProductResponseDTO> response = this.productAssembler.toModel(productResponseDTO);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  /**
   * Returns a list of all products.
   *
   * @param pageNumber the page number of the results to return (default: 1)
   * @param pageSize the number of results to return per page (default: 5)
   * @return a list of products
   */
  @Operation(
      summary = "Get all products",
      description =
          "A GET request to get all products, accessible by <b> " + "CLIENTS & ADMINS </b>",
      tags = {"Product " + "Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully found the products"),
        @ApiResponse(responseCode = "400", description = "Input validation failed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user"),
        @ApiResponse(responseCode = "404", description = "Products not" + " found"),
        @ApiResponse(responseCode = "204", description = "No products available"),
        @ApiResponse(responseCode = "500", description = "Some error " + "occurred")
      })
  @GetMapping("/v1/get/all")
  public ResponseEntity<CollectionModel<ProductResponseDTO>> getAllProducts(
      @Parameter(
              in = ParameterIn.QUERY,
              description =
                  "the page number to retrieve products if there are more than one page of data",
              schema = @Schema(minimum = "1", defaultValue = "1"))
          @RequestParam(required = false, defaultValue = "1", value = "page")
          @Min(value = 1)
          int pageNumber,
      @Parameter(
              in = ParameterIn.QUERY,
              description =
                  "the number of items per page, or the maximum number of products listed in response",
              schema = @Schema(minimum = "5", maximum = "20", defaultValue = "5"))
          @RequestParam(required = false, defaultValue = "5", value = "size")
          @Min(value = 5)
          @Max(value = 20)
          int pageSize,
      @Parameter(in = ParameterIn.QUERY, description = "search keyword to search for products")
          @RequestParam(required = false, defaultValue = "")
          String searchKey,
      @Schema(hidden = true) @RequestHeader(name = "userRole") String role)
      throws JsonProcessingException {

    Page<ProductResponseDTO> page =
        this.productService.getAllProducts(pageNumber, pageSize, searchKey, role);
    if (page.getTotalElements() != 0 && page.getContent().isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } else if (page.getTotalElements() == 0) {
      throw new ProductNotFoundException("No products founds");
    }
    CollectionModel<ProductResponseDTO> response =
        addPageMetadata(page.getContent(), page, 0, searchKey, role);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  /**
   * Get products by category
   *
   * <p>A GET request to get a list of products tagged to a specific category
   *
   * @param categoryId the id of the category to filter products by
   * @return a list of products in the specified category
   */
  @Operation(
      summary = "Get products by category",
      description =
          "A GET request to get a list of products tagged to"
              + " a specific category, "
              + "accessible by <b> CLIENTS & ADMINS </b>",
      tags = "Product Service")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully found the products"),
        @ApiResponse(responseCode = "204", description = "No products available"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user"),
        @ApiResponse(responseCode = "500", description = "Some error occurred")
      })
  @GetMapping("/v1/get/by-category/{categoryId}")
  public ResponseEntity<CollectionModel<ProductResponseDTO>> getProductsByCategory(
      @Parameter(in = ParameterIn.PATH, description = "categoryId to get products") @PathVariable
          Integer categoryId,
      @Parameter(
              in = ParameterIn.QUERY,
              description =
                  "the page number to retrieve products if there are more than one page of"
                      + " data",
              schema = @Schema(minimum = "1", defaultValue = "1"))
          @RequestParam(required = false, defaultValue = "1", value = "page")
          @Min(value = 1)
          int pageNumber,
      @Parameter(
              in = ParameterIn.QUERY,
              description =
                  "the number of items per page, or the maximum number of products listed in response",
              schema = @Schema(minimum = "5", maximum = "20", defaultValue = "5"))
          @RequestParam(required = false, defaultValue = "5", value = "size")
          @Min(value = 5)
          @Max(value = 20)
          int pageSize,
      @Parameter(in = ParameterIn.QUERY, description = "search keyword to search for products")
          @RequestParam(value = "search", required = false, defaultValue = "")
          String searchKey,
      @Schema(hidden = true) @RequestHeader(name = "userRole") String role)
      throws JsonProcessingException {

    Page<ProductResponseDTO> productsInCategory =
        this.productService.findProductsByCategory(
            categoryId, pageNumber, pageSize, searchKey, role);
    if (productsInCategory.getContent().isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    CollectionModel<ProductResponseDTO> productCollectionModel =
        addPageMetadata(
            productsInCategory.getContent(), productsInCategory, categoryId, searchKey, role);
    return new ResponseEntity<>(productCollectionModel, HttpStatus.OK);
  }

  /**
   * This endpoint is used to update the information of a specific product.
   *
   * @param productId the id of the product to update
   * @param productRequestDTO the updated information of the product
   * @return the updated product information
   */
  @Operation(
      summary = "Update product by Id",
      description =
          "A PUT request to update product by Id, accessible by " + "<b> ADMINS </b> only",
      tags = {"Product " + "Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully found the product"),
        @ApiResponse(responseCode = "400", description = "Input validation failed"),
        @ApiResponse(responseCode = "404", description = "Product Not Found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized" + " user"),
        @ApiResponse(responseCode = "500", description = "Some error " + "occurred")
      })
  @PutMapping("/v1/update/{productId}")
  public ResponseEntity<EntityModel<ProductResponseDTO>> updateProduct(
      @Schema(hidden = true) @RequestHeader(name = "loggedInUser") String username,
      @Schema(hidden = true) @RequestHeader(name = "userRole") String role,
      @PathVariable Integer productId,
      @RequestBody ProductRequestDTO productRequestDTO)
      throws Exception {

    ProductResponseDTO productResponseDTO =
        this.productService.updateProductById(productId, productRequestDTO, username, role);
    LOGGER.info("Product with Id: {} updated successfully", productId);
    EntityModel<ProductResponseDTO> entityProductModel =
        this.productAssembler.toModel(productResponseDTO);
    return new ResponseEntity<>(entityProductModel, HttpStatus.OK);
  }

  /**
   * This endpoint is used to reduce the count of a specific product.
   *
   * @param productId the id of the product to reduce the count
   * @param quantity the quantity to reduce the count
   * @return a response indicating whether the count was reduced successfully
   */
  @Operation(
      summary = "Reduce product count",
      description = "A PUT request to order a specific product, accessible" + " by all users",
      tags = {"Product Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully reduced the count"),
        @ApiResponse(responseCode = "400", description = "Input validation failed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user"),
        @ApiResponse(responseCode = "404", description = "Product Not " + "Found"),
        @ApiResponse(responseCode = "500", description = "Some error occurred")
      })
  @PutMapping("/v1/order-product/{productId}")
  public ResponseEntity<Void> orderProduct(
      @PathVariable Integer productId,
      @RequestParam String productSize,
      @RequestParam Integer quantity) {

    this.productService.reduceProductCount(productId, productSize, quantity);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * This API is used to download products data in an Excel file
   *
   * @param response to set content type and headed value
   * @throws IOException to handle Input/Output exceptions
   */
  @Operation(
      summary = "Export products data in Excel",
      description =
          "A GET request to download products list "
              + "in a excel file, accessible only by <b> ADMINS </b>",
      tags = {"Product Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully downloaded Excel file"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user"),
        @ApiResponse(responseCode = "400", description = "Request header not present"),
        @ApiResponse(responseCode = "500", description = "Some " + "Exception Occurred")
      })
  @GetMapping("/v1/export/excel")
  public void exportToExcel(
      HttpServletResponse response,
      @Schema(hidden = true) @RequestHeader(name = "userRole") String role)
      throws IOException {

    List<ProductResponseDTO> listCategories = this.productService.findProductsToExport();
    ProductExcelExporter exporter = new ProductExcelExporter();
    exporter.export(listCategories, response, role);
  }

  /**
   * This API is used to download products data in a Pdf file
   *
   * @param response to set content type and headed value
   * @throws IOException to handle Input/Output exceptions
   */
  @Operation(
      summary = "Export products data in Pdf",
      description =
          "A GET request to download products list "
              + "in"
              + " a Pdf "
              + "file, accessible only by <b> ADMINS </b>",
      tags = {"Product Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully downloaded Pdf file"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user"),
        @ApiResponse(responseCode = "400", description = "Request header not present"),
        @ApiResponse(responseCode = "500", description = "Some " + "Exception Occurred")
      })
  @GetMapping("/v1/export/pdf")
  public void exportToPdf(
      HttpServletResponse response,
      @Schema(hidden = true) @RequestHeader(name = "userRole") String role)
      throws IOException {

    List<ProductResponseDTO> listProducts = this.productService.findProductsToExport();
    ProductPdfExporter exporter = new ProductPdfExporter();
    exporter.export(listProducts, response, role);
  }

  /**
   * This API is used to upload the product image
   *
   * @param mainImage product mainImage to upload
   * @param extraImages product extraImages to upload
   * @return response object and status code
   * @throws IOException if any exception occurs
   */
  @Operation(
      summary = "Upload product's images",
      description =
          "A POST request to upload product images, " + "accessible only by <b> ADMINS </b>",
      tags = {"Product Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully uploaded images"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user"),
        @ApiResponse(responseCode = "404", description = "Product not found with specified Id"),
        @ApiResponse(responseCode = "500", description = "Some Exception Occurred")
      })
  @PostMapping(
      value = "/v1/upload-file/{productId}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<String> uploadFile(
      @RequestParam(value = "mainImage") MultipartFile mainImage,
      @RequestParam(value = "extraImages") MultipartFile[] extraImages,
      @Parameter(in = ParameterIn.PATH, description = "id of the product") @PathVariable
          Integer productId,
      @Schema(hidden = true) @RequestHeader(name = "userRole") String role,
      @Schema(hidden = true) @RequestHeader(name = "loggedInUser") String username)
      throws Exception {

    ProductResponseDTO productResponseDTO = this.productService.getProductById(productId);
    Set<String> productImages = new LinkedHashSet<>();
    for (MultipartFile multipartFile : extraImages) {
      productImages.add(multipartFile.getOriginalFilename());
    }
    FileUploadUtil.saveFile(
        productId,
        StringUtils.cleanPath(Objects.requireNonNull(mainImage.getOriginalFilename())),
        mainImage,
        role);
    FileUploadUtil.saveMultiFiles(productId, extraImages, role);
    productResponseDTO.setExtraProductImages(productImages);
    productResponseDTO.setProductMainImage(mainImage.getOriginalFilename());
    ProductRequestDTO productRequestDTO =
        this.modelMapper.map(productResponseDTO, ProductRequestDTO.class);
    this.productService.updateProductById(productId, productRequestDTO, role, username);
    LOGGER.info("Product with Id: {} updated successfully", productId);
    return new ResponseEntity<>("Image uploaded successfully!", HttpStatus.OK);
  }

  /**
   * This API is used to download the image file
   *
   * @param imageName to download
   * @return file and status code
   */
  @Operation(
      summary = "Download image of product by file name",
      description = "A GET request to get image, " + "accessible by all users",
      tags = {"Product Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully downloaded image"),
        @ApiResponse(responseCode = "404", description = "Image not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user"),
        @ApiResponse(responseCode = "500", description = "Some Exception " + "Occurred")
      })
  @GetMapping("/v1/{productId}/download-file/{imageName}")
  public ResponseEntity<?> downloadFile(
      @Parameter(in = ParameterIn.PATH, description = "id of the product")
          @PathVariable("productId")
          Integer productId,
      @Parameter(in = ParameterIn.PATH, description = "product imageName to download")
          @PathVariable("imageName")
          String imageName) {

    FileDownloadUtil downloadUtil = new FileDownloadUtil();
    Resource resource;
    try {
      resource = downloadUtil.getFileAsResource(productId, imageName);
    } catch (IOException e) {
      return ResponseEntity.internalServerError().build();
    }
    if (resource == null) {
      return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
    }
    String contentType = "application/octet-stream";
    String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
        .body(resource);
  }

  /**
   * This method is used to softly delete a product
   *
   * @param productId id of the product
   * @return ResponseEntity with status code 200 OK
   */
  @Operation(
      summary = "Delete product by product Id",
      description = "A DELETE request to delete a product, " + "accessible only by <b>ADMINS</b>",
      tags = {"Product Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully deleted product"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user"),
        @ApiResponse(responseCode = "500", description = "Some Exception " + "Occurred")
      })
  @DeleteMapping("/v1/delete/{productId}")
  public ResponseEntity<Void> deleteProduct(
      @PathVariable Integer productId,
      @Schema(hidden = true) @RequestHeader(name = "userRole") String role)
      throws Exception {

    this.productService.deleteProductById(productId, role);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * This function takes in a list of products and a page of products and returns a CollectionModel
   * of ProductResponseDTOs that includes pagination metadata. It iterates over the products list
   * and adds the links from the HATEOAS EntityModel to each ProductResponseDTO.
   *
   * @param products the list of products to add pagination metadata to
   * @param page the page of products to add pagination metadata to
   * @return a CollectionModel of ProductResponseDTOs with pagination metadata
   */
  private CollectionModel<ProductResponseDTO> addPageMetadata(
      List<ProductResponseDTO> products,
      Page<ProductResponseDTO> page,
      Integer categoryId,
      String searchKey,
      String role)
      throws JsonProcessingException {

    int pageNumber = page.getNumber() + 1; // get the current page number
    int pageSize = page.getSize(); // get the page size
    long totalElements = page.getTotalElements(); // get the total number of elements
    long totalPages = page.getTotalPages(); // get the total number of pages
    PagedModel.PageMetadata pageMetadata =
        new PagedModel.PageMetadata(
            pageSize, pageNumber, totalElements, totalPages); // create a new page metadata object
    CollectionModel<ProductResponseDTO> collectionModel = PagedModel.of(products, pageMetadata);
    for (ProductResponseDTO productResponseDTO : products) {
      EntityModel<ProductResponseDTO> model = this.productAssembler.toModel(productResponseDTO);
      productResponseDTO.add(model.getLinks());
    }
    if (categoryId == 0) {
      collectionModel.add(
          linkTo(
                  methodOn(ProductServiceController.class)
                      .getAllProducts(pageNumber, pageSize, searchKey, role))
              .withSelfRel());
      if (pageNumber > 1) {
        // add link to first page if the current page is not the first one
        collectionModel.add(
            linkTo(
                    methodOn(ProductServiceController.class)
                        .getAllProducts(1, pageSize, searchKey, role))
                .withRel(IanaLinkRelations.FIRST));
        // add link to the previous page if the current page is not the first one
        collectionModel.add(
            linkTo(
                    methodOn(ProductServiceController.class)
                        .getAllProducts(pageNumber - 1, pageSize, searchKey, role))
                .withRel(IanaLinkRelations.PREV));
      }
      if (pageNumber < totalPages) {
        // add link to next page if the current page is not the last one
        collectionModel.add(
            linkTo(
                    methodOn(ProductServiceController.class)
                        .getAllProducts(pageNumber + 1, pageSize, searchKey, role))
                .withRel(IanaLinkRelations.NEXT));
        // add link to last page if the current page is not the last one
        collectionModel.add(
            linkTo(
                    methodOn(ProductServiceController.class)
                        .getAllProducts((int) totalPages, pageSize, searchKey, role))
                .withRel(IanaLinkRelations.LAST));
      }
    } else {
      collectionModel.add(
          linkTo(
                  methodOn(ProductServiceController.class)
                      .getProductsByCategory(categoryId, pageNumber, pageSize, searchKey, role))
              .withSelfRel());
      if (pageNumber > 1) {
        // add link to first page if the current page is not the first one
        collectionModel.add(
            linkTo(
                    methodOn(ProductServiceController.class)
                        .getProductsByCategory(categoryId, 1, pageSize, searchKey, role))
                .withRel(IanaLinkRelations.FIRST));
        // add link to the previous page if the current page is not the first one
        collectionModel.add(
            linkTo(
                    methodOn(ProductServiceController.class)
                        .getProductsByCategory(
                            categoryId, pageNumber - 1, pageSize, searchKey, role))
                .withRel(IanaLinkRelations.PREV));
      }
      if (pageNumber < totalPages) {
        // add link to next page if the current page is not the last one
        collectionModel.add(
            linkTo(
                    methodOn(ProductServiceController.class)
                        .getProductsByCategory(
                            categoryId, pageNumber + 1, pageSize, searchKey, role))
                .withRel(IanaLinkRelations.NEXT));
        // add link to last page if the current page is not the last one
        collectionModel.add(
            linkTo(
                    methodOn(ProductServiceController.class)
                        .getProductsByCategory(
                            categoryId, (int) totalPages, pageSize, searchKey, role))
                .withRel(IanaLinkRelations.LAST));
      }
    }
    return collectionModel;
  }
}
