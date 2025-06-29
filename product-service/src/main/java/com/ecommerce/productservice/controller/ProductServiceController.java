package com.ecommerce.productservice.controller;

import static java.util.Objects.isNull;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.ecommerce.productservice.exception.ProductNotFoundException;
import com.ecommerce.productservice.payload.request.OrderProductDTO;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * The ProductServiceController class is responsible for handling HTTP requests related to product
 * operations. This includes adding, retrieving, updating, and reducing the count of products, as
 * well as filtering products by category. It provides a RESTful API for managing product data,
 * accessible to specific roles based on authorization.
 *
 * <p>Fields: - LOGGER: Used for logging relevant information and activities within the class. -
 * productService: Service to handle the business logic related to product operations. -
 * productAssembler: Used to create response representations of product entities. - modelMapper:
 * Utility for object mapping between different data representations.
 *
 * <p>Methods: - addProduct: Adds a new product to the database under a specified category. -
 * getProductById: Retrieves a product based on its unique identifier. - getAllProducts: Fetches a
 * paginated list of all products, with optional search functionality. - getProductsByCategory:
 * Retrieves a paginated list of products associated with a specific category, with optional search
 * support. - updateProduct: Updates an existing product's details based on its ID. -
 * reduceProductCount: Decreases the count of specified products, typically used for order
 * processing.
 */
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
   * Handles the addition of a new product to the specified category. This endpoint is accessible
   * only by users with admin roles.
   *
   * @param username the username of the user making the request, extracted from the request header
   * @param role the role of the user making the request, extracted from the request header
   * @param productRequestDTO the product data provided in the request body to be added
   * @param categoryId the ID of the category in which the product will be added, provided as a path
   *     variable
   * @return a ResponseEntity containing the added product as a resource and an HTTP status of 201
   *     (Created)
   * @throws Exception if an error occurs during the product creation process
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
      @Schema(hidden = true) @RequestHeader(name = "username") String username,
      @Schema(hidden = true) @RequestHeader(name = "role") String role,
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
   * Retrieves a product by its unique identifier.
   *
   * <p>A GET request to fetch a product using its ID. This endpoint is accessible by both clients
   * and administrators.
   *
   * @param productId the unique identifier of the product to be retrieved
   * @return a ResponseEntity containing an EntityModel of ProductResponseDTO if the product is
   *     found successfully, along with the appropriate HTTP status code
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
   * Retrieves all products with optional pagination and search functionality. Provides a pageable
   * list of products that match the specified criteria.
   *
   * @param pageNumber the page number to retrieve, must be greater than or equal to 1, default is 1
   * @param pageSize the number of products per page, must be between 5 and 20, default is 5
   * @param searchKey an optional search keyword to filter products, default is an empty string
   * @return a {@link ResponseEntity} containing a {@link CollectionModel} of {@link
   *     ProductResponseDTO} representing the products found, along with HTTP status code
   * @throws JsonProcessingException if any JSON processing errors occur
   */
  @Operation(
      summary = "Get all products",
      description = "A GET request to get all products, accessible by all users",
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
          String searchKey)
      throws JsonProcessingException {

    Page<ProductResponseDTO> page =
        this.productService.getAllProducts(pageNumber, pageSize, searchKey);
    if (page.getTotalElements() != 0 && page.getContent().isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } else if (page.getTotalElements() == 0) {
      throw new ProductNotFoundException("No products founds");
    }
    CollectionModel<ProductResponseDTO> response =
        addPageMetadata(page.getContent(), page, 0, searchKey, null);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  /**
   * Retrieves a list of products associated with a specific category. The results can be paginated
   * and optionally filtered by a search keyword. Accessible to users with valid roles such as
   * CLIENTS and ADMINS.
   *
   * @param categoryId the ID of the category for which products are to be retrieved
   * @param pageNumber the page number of the results to retrieve, starting from 1
   * @param pageSize the number of products to retrieve per page, with a minimum of 5 and a maximum
   *     of 20
   * @param searchKey an optional keyword to filter the products by a search query
   * @param role the user role provided in the request header to determine access permissions
   * @return a ResponseEntity containing a CollectionModel of ProductResponseDTO if products are
   *     found; returns a status of 204 (no content) if no products are found
   * @throws JsonProcessingException if there is an error processing JSON data
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
   * Updates an existing product identified by its ID. This method is only accessible to users with
   * admin privileges, allowing them to modify the product's details using the provided information.
   *
   * @param username the username of the authenticated user making the request
   * @param role the role of the authenticated user making the request
   * @param productId the ID of the product to be updated
   * @param productRequestDTO the updated product details encapsulated in a request DTO
   * @return a ResponseEntity containing the updated product details wrapped in an EntityModel
   * @throws Exception if an error occurs during the update process
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
      @Schema(hidden = true) @RequestHeader(name = "username") String username,
      @Schema(hidden = true) @RequestHeader(name = "role") String role,
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
   * Reduces the count of specified products by processing an order.
   *
   * @param products a list of products specified through OrderProductDTO, each containing
   *     information about the product and the quantity to order
   * @return ResponseEntity<Void> indicating the status of the operation; HTTP status codes used
   *     include 200 (success), 400 (validation error), 401 (unauthorized user), 404 (product not
   *     found), and 500 (server error)
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
  @PutMapping("/v1/order")
  public ResponseEntity<Void> orderProduct(@RequestBody List<OrderProductDTO> products) {

    this.productService.reduceProductCount(products);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Releases the reserved stock for the given list of products.
   *
   * @param products a list of {@code OrderProductDTO} objects representing the products whose
   *     reserved stock should be released
   * @return a {@code ResponseEntity<Void>} indicating the HTTP response status
   */
  @PostMapping("/v1/reserved-stocks/release")
  public ResponseEntity<Void> releaseReservedStock(@RequestBody List<OrderProductDTO> products) {
    this.productService.releaseReservedProductCount(products);
    return ResponseEntity.ok().build();
  }

  /**
   * Confirms the stock availability for the given list of products by their count.
   *
   * @param products a list of {@code OrderProductDTO} objects representing the products to confirm
   *     stock count for
   * @return a {@code ResponseEntity<Void>} indicating the operation's success
   */
  @PostMapping("/v1/confirm-stocks/count")
  public ResponseEntity<Void> confirmProductCount(@RequestBody List<OrderProductDTO> products) {
    this.productService.confirmProductCount(products);
    return ResponseEntity.ok().build();
  }

  /**
   * Exports the list of products to an Excel file.
   *
   * @param response the HTTP response to which the Excel file will be written
   * @param role the role of the user making the request, which must be "ADMIN"
   * @throws IOException if an I/O error occurs during the export process
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
   * Exports the list of products to a PDF file. This method is accessible only to users with admin
   * privileges. The PDF file is generated and written to the provided HTTP response.
   *
   * @param response the HttpServletResponse instance to write the PDF file to
   * @param role the user role passed via the request header, used to authorize the operation
   * @throws IOException if an input or output exception occurs during the PDF generation process
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
   * Uploads images for a specified product. The method allows uploading a main image and additional
   * extra images for a specific product by its unique ID. This operation is accessible only to
   * users with administrative roles.
   *
   * @param mainImage the main image file to be uploaded for the product
   * @param extraImages an array of additional image files to be uploaded for the product
   * @param productId the unique identifier of the product
   * @param role the role of the user performing the operation, typically "ADMIN"
   * @param username the username of the user performing the operation
   * @return a ResponseEntity containing a success message upon successful upload of the images
   * @throws Exception if any error occurs during the process of retrieving the product, saving the
   *     images, or updating the product
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
      @Schema(hidden = true) @RequestHeader(name = "role") String role,
      @Schema(hidden = true) @RequestHeader(name = "username") String username)
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
   * Downloads the image file of a product based on the provided file name. This method handles GET
   * requests and provides the specified file as a resource for download. The file is identified
   * using the product ID and the image name.
   *
   * @param productId The unique identifier of the product whose image is to be downloaded.
   * @param imageName The name of the image file to be downloaded.
   * @return A ResponseEntity containing the requested file as a resource if successful, or an
   *     appropriate HTTP status (e.g., 404 if the file is not found, 500 for server errors).
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
   * Deletes a product by the specified product ID. Only users with admin roles are authorized to
   * perform this operation.
   *
   * @param productId the unique identifier of the product to be deleted
   * @param role the role of the user making the request, used to verify authorization
   * @return ResponseEntity with HTTP status 200 if the product is successfully deleted
   * @throws Exception if an error occurs during the deletion process
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
   * Adds pagination metadata and hypermedia links to a list of products.
   *
   * @param products the list of products to be included in the paginated response
   * @param page the page object containing pagination details such as current page, size, etc.
   * @param categoryId the category ID to filter the products, where 0 means no category filter
   * @param searchKey the search term for filtering products
   * @param role the role of the user for filtering or accessing specific products
   * @return a CollectionModel containing the paginated products along with metadata and links
   * @throws JsonProcessingException if there is an error during JSON processing
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
    if (categoryId == 0 && isNull(role)) {
      collectionModel.add(
          linkTo(
                  methodOn(ProductServiceController.class)
                      .getAllProducts(pageNumber, pageSize, searchKey))
              .withSelfRel());
      if (pageNumber > 1) {
        // add link to first page if the current page is not the first one
        collectionModel.add(
            linkTo(methodOn(ProductServiceController.class).getAllProducts(1, pageSize, searchKey))
                .withRel(IanaLinkRelations.FIRST));
        // add link to the previous page if the current page is not the first one
        collectionModel.add(
            linkTo(
                    methodOn(ProductServiceController.class)
                        .getAllProducts(pageNumber - 1, pageSize, searchKey))
                .withRel(IanaLinkRelations.PREV));
      }
      if (pageNumber < totalPages) {
        // add link to next page if the current page is not the last one
        collectionModel.add(
            linkTo(
                    methodOn(ProductServiceController.class)
                        .getAllProducts(pageNumber + 1, pageSize, searchKey))
                .withRel(IanaLinkRelations.NEXT));
        // add link to last page if the current page is not the last one
        collectionModel.add(
            linkTo(
                    methodOn(ProductServiceController.class)
                        .getAllProducts((int) totalPages, pageSize, searchKey))
                .withRel(IanaLinkRelations.LAST));
      }
    } else if (categoryId != 0 && !isNull(role)) {
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
