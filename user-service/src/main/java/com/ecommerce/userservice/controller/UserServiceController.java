package com.ecommerce.userservice.controller;

import static java.util.Objects.requireNonNull;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.ecommerce.userservice.model.Role;
import com.ecommerce.userservice.payload.request.UpdateUserDTO;
import com.ecommerce.userservice.payload.request.UserDTO;
import com.ecommerce.userservice.payload.response.ExceptionInResponse;
import com.ecommerce.userservice.payload.response.UserDTOResponse;
import com.ecommerce.userservice.service.UserService;
import com.ecommerce.userservice.service.export.UserExcelExporter;
import com.ecommerce.userservice.service.export.UserPdfExporter;
import com.ecommerce.userservice.util.FileDownloadUtil;
import com.ecommerce.userservice.util.FileUploadUtil;
import com.ecommerce.userservice.util.UserModelAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.IOException;
import java.util.List;
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
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * UserServiceController is a REST controller for managing user-related operations. It includes APIs
 * to retrieve, update, and delete users while enforcing role-based access controls for operations
 * accessible by ADMINS and USERS.
 *
 * <p>Fields: - LOGGER: Logger instance for logging operations and exceptions. - userService:
 * Service layer dependency managing business logic for user operations. - userModelAssembler:
 * Assembler for transforming user entities into API response models. - modelMapper: Utility for
 * mapping between DTOs and entity objects.
 */
@RestController
@Tag(name = "User Service", description = "Admins should use this service to get/read all users")
@RequiredArgsConstructor
@Validated
public class UserServiceController {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceController.class);

  private final UserService userService;

  private final UserModelAssembler userModelAssembler;

  private final ModelMapper modelMapper;

  /**
   * Retrieves a user by their email address, accessible only by admins.
   *
   * @param emailId the email address of the user to be retrieved
   * @return a {@code ResponseEntity} containing an {@code EntityModel} of {@code UserDTOResponse}
   *     if the user is found
   */
  @Operation(
      summary = "Get user by username managed by admins",
      description = "A GET request to get user by " + "username accessible by <b>ADMINS</b>",
      tags = {"User" + " Service"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully found the user",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserDTOResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized Access",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Some Exception Occurred",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class)))
      })
  @GetMapping(value = "/v1/get/{username}")
  public ResponseEntity<EntityModel<UserDTOResponse>> getUserByEmail(
      @Parameter(required = true, in = ParameterIn.PATH, description = "emailId of the user")
          @PathVariable("username")
          String emailId) {

    UserDTOResponse userDTOResponse = this.userService.getUserByUsername(emailId);
    EntityModel<UserDTOResponse> entityModel = this.userModelAssembler.toModel(userDTOResponse);
    return new ResponseEntity<>(entityModel, HttpStatus.OK);
  }

  /**
   * Retrieves all users managed by admins, with optional pagination and filtering support.
   *
   * @param pageNumber the page number to retrieve (minimum value is 1, default value is 1)
   * @param pageSize the number of items per page (minimum value is 5 and maximum value is 20,
   *     default value is 5)
   * @param query the field name to filter the response by
   * @return a ResponseEntity containing the collection of users with associated metadata, or
   *     appropriate HTTP status if no users are found
   */
  @Operation(
      summary = "Get all users managed by admins",
      description = "A GET request to get all users accessible " + "by <b>ADMINS</b>",
      tags = {"User Service"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully found all the users",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserDTOResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "No Users Found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized " + "Access",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class))),
        @ApiResponse(
            responseCode = "204",
            description = "No users" + " found on this page",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema())),
        @ApiResponse(
            responseCode = "500",
            description = "Some Exception Occurred",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class)))
      })
  @GetMapping("/v1/all")
  public ResponseEntity<CollectionModel<UserDTOResponse>> getAllUsers(
      @Parameter(
              in = ParameterIn.QUERY,
              description =
                  "the page number to retrieve users if there are more than one page of data",
              schema = @Schema(minimum = "1", defaultValue = "1"))
          @RequestParam(required = false, defaultValue = "1", value = "page")
          @Min(value = 1)
          int pageNumber,
      @Parameter(
              in = ParameterIn.QUERY,
              description =
                  "the number of items per page, or the maximum number of users listed in response",
              schema = @Schema(minimum = "5", maximum = "20", defaultValue = "5"))
          @RequestParam(required = false, defaultValue = "5", value = "size")
          @Min(value = 5)
          @Max(value = 20)
          int pageSize,
      @Parameter(
              in = ParameterIn.QUERY,
              description = "the field name with which you want to filter the response",
              schema = @Schema())
          @RequestParam(required = false, defaultValue = "", value = "search")
          String query) {

    Page<UserDTOResponse> users = this.userService.usersList(pageNumber, pageSize, query);
    CollectionModel<UserDTOResponse> collectionModel =
        addPageMetadata(users.getContent(), users, query);
    if (collectionModel.getContent().isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    return new ResponseEntity<>(collectionModel, HttpStatus.OK);
  }

  /**
   * Updates an existing user based on the provided user details.
   *
   * @param updateUserDTO The data transfer object containing the updated user details.
   * @return A ResponseEntity containing the EntityModel of the updated UserDTOResponse. Returns a
   *     200 status code if the update is successful. Possible errors include: - 401 Unauthorized
   *     access - 404 User not found - 500 Internal server error
   */
  @Operation(
      summary = "Update user by username managed by all users",
      description = "A PUT request to update user " + "accessible by all <b>USERS</b>",
      tags = {"User " + "Service"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully updated the user",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserDTOResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized Access",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Some Exception " + "Occurred",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class)))
      })
  @PutMapping("/v1/update")
  public ResponseEntity<EntityModel<UserDTOResponse>> userUpdate(
      @Valid @RequestBody UpdateUserDTO updateUserDTO) {

    UserDTOResponse updatedUser =
        this.userService.updateUser(updateUserDTO, updateUserDTO.getEmailId());
    EntityModel<UserDTOResponse> entityModel = this.userModelAssembler.toModel(updatedUser);
    LOGGER.info("{}", "***** User updated successfully *****");
    return new ResponseEntity<>(entityModel, HttpStatusCode.valueOf(200));
  }

  /**
   * Updates the role of a user identified by their username. This operation is accessible only by
   * administrators.
   *
   * @param username the username of the user whose role is to be updated
   * @param role the new role to be assigned to the user
   * @return a ResponseEntity containing an EntityModel of UserDTOResponse if the update is
   *     successful
   */
  @Operation(
      summary = "Update user by user's role",
      description = "A PATCH request to update user's role " + "accessible by <b>ADMINS</b>",
      tags = {"User Service"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully updated the user's role",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserDTOResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Input validation " + "failed",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = MethodArgumentNotValidException.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized Access",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Some Exception Occurred",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class)))
      })
  @PatchMapping("/v1/role/update/{username}/{role}")
  public ResponseEntity<EntityModel<UserDTOResponse>> updateUserRole(
      @PathVariable String username, @PathVariable String role) {

    UserDTOResponse userDTOResponse = this.userService.updateRole(username, Role.valueOf(role));
    EntityModel<UserDTOResponse> entityModel = this.userModelAssembler.toModel(userDTOResponse);
    LOGGER.info("{}", "***** User role updated successfully *****");
    return new ResponseEntity<>(entityModel, HttpStatusCode.valueOf(200));
  }

  /**
   * Deletes a user identified by the provided username.
   *
   * <p>This method handles HTTP DELETE requests to remove a specific user from the system,
   * accessible only by users with admin-level privileges.
   *
   * @param emailId the username of the user to be deleted
   * @return a ResponseEntity containing a success message with HTTP 200 status if the deletion is
   *     successful
   */
  @Operation(
      summary = "Delete user by username",
      description = "A DELETE request to delete user accessible by " + "<b>ADMINS</b>",
      tags = {"User " + "Service"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully deleted the user",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class))),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized Access",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Some Exception Occurred",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class)))
      })
  @DeleteMapping("/v1/delete/{username}")
  public ResponseEntity<String> userDelete(@PathVariable("username") String emailId) {

    String deletedUser = this.userService.deleteUser(emailId);
    LOGGER.info("{}", "***** User deleted successfully *****");
    return new ResponseEntity<>(deletedUser, HttpStatus.OK);
  }

  /**
   * Uploads a display picture for a user and updates the user's information with the new file name.
   *
   * @param multipartFile the image file to be uploaded
   * @param username the username of the user whose display picture is being updated
   * @return a ResponseEntity object with a success message and HTTP status code 200 on successful
   *     upload
   * @throws IOException if an error occurs during file upload or processing
   */
  @Operation(
      summary = "Upload user's display picture",
      description = "A POST request to upload image accessible by" + " all users",
      tags = {"User Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully uploaded image"),
        @ApiResponse(responseCode = "500", description = "Some Exception Occurred")
      })
  @PostMapping(value = "/v1/upload-file/{username}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<String> uploadFile(
      @RequestParam("file") MultipartFile multipartFile, @PathVariable String username)
      throws IOException {

    UserDTOResponse userDTOResponse = this.userService.getUserByUsername(username);
    String fileName = StringUtils.cleanPath(requireNonNull(multipartFile.getOriginalFilename()));
    FileUploadUtil.saveFile(fileName, multipartFile);
    LOGGER.info("***** User image saved successfully *****");
    userDTOResponse.setAvatarName(fileName);
    UserDTO userDTO = this.modelMapper.map(userDTOResponse, UserDTO.class);
    UpdateUserDTO updateUserDTO = this.modelMapper.map(userDTO, UpdateUserDTO.class);
    this.userService.updateUser(updateUserDTO, username);
    LOGGER.info("***** Image uploaded successfully! *****");
    return new ResponseEntity<>("Image uploaded successfully 🙂", HttpStatus.OK);
  }

  /**
   * Downloads the file associated with the specified avatar name. This method allows authorized
   * administrators to retrieve a file resource, typically an image, by its corresponding file code.
   *
   * @param avatarName the name of the avatar file to be downloaded
   * @return a ResponseEntity containing the file resource. If the file is found, the response
   *     contains the file as an attachment with a 200 status code. If the file is not found, a
   *     response with a 404 status code is returned. In the case of unauthorized access or server
   *     errors, appropriate response codes (401 or 500) are returned.
   */
  @Operation(
      summary = "Download image of user by file code",
      description = "A GET request to get image accessible " + "by <b>ADMINS</b>",
      tags = {"User Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully downloaded image"),
        @ApiResponse(responseCode = "404", description = "Image not found"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized Access",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class))),
        @ApiResponse(responseCode = "500", description = "Some Exception Occurred")
      })
  @GetMapping("/v1/download-file/{avatarName}")
  public ResponseEntity<?> downloadFile(@PathVariable String avatarName) {

    FileDownloadUtil downloadUtil = new FileDownloadUtil();
    Resource resource = null;
    try {
      resource = downloadUtil.getFileAsResource(avatarName);
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
   * Exports a list of users to an Excel file, available only to users with an admin role. The
   * exported Excel file is written directly to the HTTP response stream.
   *
   * @param response the HttpServletResponse to write the Excel file to
   * @param role the role of the user making the request, expected to be "admin"
   * @throws IOException if an I/O error occurs while generating or writing the Excel file
   */
  @Operation(
      summary = "Export user's data in Excel",
      description =
          "A GET request to download user's list in a " + "Excel file accessible by <b>ADMINS</b>",
      tags = {"User " + "Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully found all the users"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized Access",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Some Exception Occurred",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class)))
      })
  @GetMapping("/v1/export/excel")
  public void exportToExcel(
      HttpServletResponse response,
      @Schema(hidden = true) @RequestHeader(name = "role") String role)
      throws IOException {

    List<UserDTOResponse> listUsers = this.userService.listAll();
    UserExcelExporter exporter = new UserExcelExporter();
    exporter.export(listUsers, response, role);
  }

  /**
   * Exports the list of users to a PDF file. This method generates a PDF document containing the
   * user's data and sends it as a response. Access to this endpoint is restricted to users with the
   * role of <b>ADMINS</b>.
   *
   * @param response The {@link HttpServletResponse} object used to write the PDF content to the
   *     HTTP response.
   * @param role The role of the requesting user, provided in the request header.
   * @throws IOException If an input or output exception occurs while processing the PDF export.
   */
  @Operation(
      summary = "Export user's data in Pdf",
      description =
          "A GET request to download user's list in a Pdf " + "file accessible by <b>ADMINS</b>",
      tags = {"User " + "Service"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully found all the users"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized Access",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Some Exception Occurred",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class)))
      })
  @GetMapping("/v1/export/pdf")
  public void exportToPdf(
      HttpServletResponse response,
      @Schema(hidden = true) @RequestHeader(name = "role") String role)
      throws IOException {

    List<UserDTOResponse> listUsers = this.userService.listAll();
    UserPdfExporter exporter = new UserPdfExporter();
    exporter.export(listUsers, response, role);
  }

  /**
   * Adds pagination metadata and navigational links to the collection of user responses.
   *
   * @param users the list of user response objects to be included in the collection
   * @param page the page object containing pagination information such as page number, size, and
   *     total counts
   * @param searchKey the search key used to filter the user results
   * @return a collection model of user response objects, enriched with pagination metadata and
   *     navigational links for self, first, previous, next, and last pages
   */
  private CollectionModel<UserDTOResponse> addPageMetadata(
      List<UserDTOResponse> users, Page<UserDTOResponse> page, String searchKey) {

    int pageNumber = page.getNumber() + 1;
    int pageSize = page.getSize();
    long totalElements = page.getTotalElements();
    long totalPages = page.getTotalPages();
    PagedModel.PageMetadata pageMetadata =
        new PagedModel.PageMetadata(pageSize, pageNumber, totalElements, totalPages);
    CollectionModel<UserDTOResponse> collectionModel = PagedModel.of(users, pageMetadata);
    for (UserDTOResponse userDTOResponse : users) {
      EntityModel<UserDTOResponse> model = this.userModelAssembler.toModel(userDTOResponse);
      userDTOResponse.add(model.getLinks());
    }
    collectionModel.add(
        linkTo(methodOn(UserServiceController.class).getAllUsers(pageNumber, pageSize, searchKey))
            .withSelfRel());
    if (pageNumber > 1) {
      // add link to first page if the current page is not the first one
      collectionModel.add(
          linkTo(methodOn(UserServiceController.class).getAllUsers(1, pageSize, searchKey))
              .withRel(IanaLinkRelations.FIRST));
      // add link to the previous page if the current page is not the first one
      collectionModel.add(
          linkTo(
                  methodOn(UserServiceController.class)
                      .getAllUsers(pageNumber - 1, pageSize, searchKey))
              .withRel(IanaLinkRelations.PREV));
    }
    if (pageNumber < totalPages) {
      // add link to next page if the current page is not the last one
      collectionModel.add(
          linkTo(
                  methodOn(UserServiceController.class)
                      .getAllUsers(pageNumber + 1, pageSize, searchKey))
              .withRel(IanaLinkRelations.NEXT));
      // add link to last page if the current page is not the last one
      collectionModel.add(
          linkTo(
                  methodOn(UserServiceController.class)
                      .getAllUsers((int) totalPages, pageSize, searchKey))
              .withRel(IanaLinkRelations.LAST));
    }
    return collectionModel;
  }
}
