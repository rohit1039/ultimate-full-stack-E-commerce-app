package com.ecommerce.categoryservice.controller;

import static com.ecommerce.categoryservice.util.DynamicFiltering.*;

import com.ecommerce.categoryservice.payload.request.CategoryRequestDTO;
import com.ecommerce.categoryservice.payload.response.CategoryResponseDTO;
import com.ecommerce.categoryservice.service.CategoryService;
import com.ecommerce.categoryservice.service.export.CategoryExcelExporter;
import com.ecommerce.categoryservice.service.export.CategoryPdfExporter;
import com.ecommerce.categoryservice.util.CategoryModelAssembler;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(
    name = "Category Service",
    description =
        "Clients should use this service to get category's details only, "
            + "and Admins should use this service to add, get, update and delete category details")
@Validated
public class CategoryServiceController {

  private static final Logger LOGGER = LoggerFactory.getLogger(CategoryServiceController.class);

  private final CategoryService categoryService;

  private final CategoryModelAssembler categoryModelAssembler;

  private final ModelMapper modelMapper;

  /**
   * This API is used to create a category, and save into the database.
   *
   * @param categoryDTO receive request from payload
   * @return model based response and status code
   */
  @Operation(
      summary = "Create Category",
      description =
          "A POST request to create & save categories, accessible " + "only by <b> ADMINS </b>",
      tags = "Category Service")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Successfully created the category"),
        @ApiResponse(responseCode = "400", description = "Input validation failed"),
        @ApiResponse(responseCode = "404", description = "Parent category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user"),
        @ApiResponse(responseCode = "409", description = "Categories cannot be duplicated"),
        @ApiResponse(responseCode = "500", description = "Some error occurred")
      })
  @PostMapping("/v1/add")
  public ResponseEntity<MappingJacksonValue> createCategory(
      @Valid @RequestBody CategoryRequestDTO categoryDTO,
      @Schema(hidden = true) @RequestHeader(name = "loggedInUser") String username,
      @Schema(hidden = true) @RequestHeader(name = "userRole") String role)
      throws Exception {

    CategoryResponseDTO savedCategory =
        this.categoryService.saveCategory(categoryDTO, username, role);
    MappingJacksonValue map = modelToJackson(savedCategory, ignorableFieldNamesInCreateCategory);
    return new ResponseEntity<>(map, HttpStatus.CREATED);
  }

  /**
   * This API is used to get list of categories
   *
   * @return - embedded models based response with status code
   */
  @Operation(
      summary = "Get all managed categories",
      description = "A GET request to get list of categories, " + "accessible by all users",
      tags = "Category Service")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully found the categories"),
        @ApiResponse(responseCode = "404", description = "No categories found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user"),
        @ApiResponse(responseCode = "500", description = "Some error " + "occurred")
      })
  @GetMapping("/v1/all")
  public ResponseEntity<MappingJacksonValue> getAllCategories(
      @Schema(hidden = true) @RequestHeader(name = "userRole") String role,
      @Parameter(in = ParameterIn.QUERY, description = "filter with name of the " + "category")
          @RequestParam(value = "category_name", required = false, defaultValue = "")
          String categoryName,
      @Parameter(in = ParameterIn.QUERY, description = "filter with category created by")
          @RequestParam(value = "created_by", required = false, defaultValue = "")
          String createdBy,
      @Parameter(in = ParameterIn.QUERY, description = "filter with category enabled or disabled")
          @RequestParam(value = "enabled", required = false, defaultValue = "")
          String enabled) {

    Map<String, Object> filterFields = new HashMap<>();
    if (!"".equals(enabled)) {
      filterFields.put("enabled", Boolean.parseBoolean(enabled));
    }
    if (!"".equals(createdBy)) {
      filterFields.put("createdBy", createdBy);
    }
    if (!"".equals(categoryName)) {
      filterFields.put("categoryName", categoryName);
    }
    List<CategoryResponseDTO> savedCategory =
        this.categoryService.findAllCategories(filterFields, role);
    CollectionModel<EntityModel<CategoryResponseDTO>> collectionModel =
        categoryModelAssembler.toCollectionModel(savedCategory);
    SimpleFilterProvider simpleFilterProvider =
        new SimpleFilterProvider().setFailOnUnknownId(false);
    FilterProvider filter =
        simpleFilterProvider.addFilter(
            "filter_parent",
            SimpleBeanPropertyFilter.serializeAllExcept(ignorableFieldNamesInGetCategories));
    MappingJacksonValue map = new MappingJacksonValue(collectionModel);
    map.setFilters(filter);
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  /**
   * This API is used to get the details of the user by username
   *
   * @param categoryId Get the details of the category by categoryId
   * @return category if exists with status code 200 OK
   */
  @Operation(
      summary = "Get category by Id",
      description = "A GET request to get category by Id, accessible only by" + "  <b> ADMINS </b>",
      tags = "Category " + "Service")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully found the category"),
        @ApiResponse(responseCode = "404", description = "No categories found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user"),
        @ApiResponse(responseCode = "500", description = "Some error " + "occurred")
      })
  @GetMapping("/v1/get/{categoryId}")
  public ResponseEntity<MappingJacksonValue> getCategoryById(
      @Parameter(in = ParameterIn.PATH, description = "categoryId to get category")
          @Min(value = 1, message = "cannot be zero or negative")
          @PathVariable
          Long categoryId) {

    CategoryResponseDTO category = this.categoryService.findCategoryById(categoryId);
    MappingJacksonValue map = modelToJackson(category, ignorableFieldNamesInGetCategoryById);
    LOGGER.info("*** {}: {} ***", "Category found with ID", categoryId);
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  /**
   * This API is used to download categories data in an Excel file
   *
   * @param response to set content type and headed value
   * @throws IOException to handle Input/Output exceptions
   */
  @Operation(
      summary = "Export categories data in Excel",
      description =
          "A GET request to download categories list "
              + "in a excel file, accessible only by <b> ADMINS </b>",
      tags = {"Category Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully found all the categories"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user"),
        @ApiResponse(responseCode = "400", description = "Request header not present"),
        @ApiResponse(responseCode = "500", description = "Some " + "Exception Occurred")
      })
  @GetMapping("/v1/export/excel")
  public void exportToExcel(
      HttpServletResponse response,
      @Schema(hidden = true) @RequestHeader(name = "userRole") String role)
      throws IOException {

    List<CategoryResponseDTO> listCategories =
        this.categoryService.findAllCategories(new HashMap<>(), "");
    CategoryExcelExporter exporter = new CategoryExcelExporter();
    exporter.export(listCategories, response, role);
  }

  /**
   * This API is used to download categories data in a Pdf file
   *
   * @param response to set content type and headed value
   * @throws IOException to handle Input/Output exceptions
   */
  @Operation(
      summary = "Export categories data in Pdf",
      description =
          "A GET request to download categories list "
              + "in"
              + " a Pdf "
              + "file, accessible only by <b> ADMINS "
              + "</b>",
      tags = {"Category Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully found all the categories"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user"),
        @ApiResponse(responseCode = "400", description = "Request header not present"),
        @ApiResponse(responseCode = "500", description = "Some " + "Exception Occurred")
      })
  @GetMapping("/v1/export/pdf")
  public void exportToPdf(
      HttpServletResponse response,
      @Schema(hidden = true) @RequestHeader(name = "userRole") String role)
      throws IOException {

    List<CategoryResponseDTO> listCategories =
        this.categoryService.findAllCategories(new HashMap<>(), "");
    CategoryPdfExporter exporter = new CategoryPdfExporter();
    exporter.export(listCategories, response, role);
  }

  /**
   * This API is used to update the category with the values provided on the payload
   *
   * @param categoryRequestDTO get fields that needs to be updated
   * @param categoryId which category needs to be updated
   * @return updatedUser and status code
   */
  @Operation(
      summary = "Update Category",
      description = "A PUT request to update category, accessible only by <b> " + "ADMINS </b>",
      tags = {"Category " + "Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated the category"),
        @ApiResponse(responseCode = "400", description = "Input validation failed"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user"),
        @ApiResponse(responseCode = "409", description = "Categories cannot be duplicated"),
        @ApiResponse(responseCode = "500", description = "Some error occurred")
      })
  @PutMapping("/v1/update/{categoryId}")
  public ResponseEntity<MappingJacksonValue> updateCategory(
      @Parameter(in = ParameterIn.PATH, description = "categoryId to get category")
          @Min(value = 1, message = "cannot be zero or negative")
          @PathVariable
          Long categoryId,
      @Valid @RequestBody CategoryRequestDTO categoryRequestDTO,
      @Schema(hidden = true) @RequestHeader(name = "loggedInUser") String username,
      @Schema(hidden = true) @RequestHeader(name = "userRole") String role)
      throws Exception {

    CategoryResponseDTO categoryResponseDTO =
        this.categoryService.updateCategory(categoryId, categoryRequestDTO, username, role);
    MappingJacksonValue map =
        modelToJackson(categoryResponseDTO, ignorableFieldNamesInGetCategoryById);
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  /**
   * This API is used to update the category's status as enabled or disabled
   *
   * @param categoryId which category to update
   * @param status true/false
   * @return status code
   */
  @Operation(
      summary = "Update status of a category",
      description =
          "A PATCH request to update a category, "
              + "accessible only by <b> ADMINS </b> "
              + "categories",
      tags = {"Category Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully enabled/disabled category"),
        @ApiResponse(responseCode = "401", description = "Unauthorized user"),
        @ApiResponse(responseCode = "500", description = "Some Exception Occurred")
      })
  @PatchMapping("/v1/update/{categoryId}/status")
  public ResponseEntity<?> updateStatusCategoryById(
      @Parameter(in = ParameterIn.PATH, description = "categoryId to " + "get category")
          @Min(value = 1, message = "cannot be zero or negative")
          @PathVariable
          Long categoryId,
      @RequestParam("status") boolean status,
      @Schema(hidden = true) @RequestHeader(name = "loggedInUser") String username,
      @Schema(hidden = true) @RequestHeader(name = "userRole") String role)
      throws Exception {

    this.categoryService.updateStatusCategory(categoryId, status, username, role);
    return ResponseEntity.ok().build();
  }

  /**
   * This method takes a CategoryResponseDTO object and returns a MappingJacksonValue object that
   * contains the serialized EntityModel of the CategoryResponseDTO object. The method uses the
   * SimpleBeanPropertyFilter class to filter out the properties that should not be serialized.
   *
   * @param savedCategory the CategoryResponseDTO object to be serialized
   * @param ignorableFields a set of field names that should be ignored during serialization
   * @return a MappingJacksonValue object that contains the serialized EntityModel of the
   *     CategoryResponseDTO object
   */
  private MappingJacksonValue modelToJackson(
      CategoryResponseDTO savedCategory, Set<String> ignorableFields) {

    EntityModel<CategoryResponseDTO> entityModel =
        this.categoryModelAssembler.toModel(savedCategory);
    SimpleFilterProvider simpleFilterProvider =
        new SimpleFilterProvider().setFailOnUnknownId(false);
    FilterProvider filter =
        simpleFilterProvider.addFilter(
            "filter_parent", SimpleBeanPropertyFilter.serializeAllExcept(ignorableFields));
    MappingJacksonValue map = new MappingJacksonValue(entityModel);
    map.setFilters(filter);
    return map;
  }
}
