package com.ecommerce.userservice.controller;

import static java.util.Objects.requireNonNull;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.ecommerce.userservice.model.Role;
import com.ecommerce.userservice.payload.request.UpdateUserDTO;
import com.ecommerce.userservice.payload.request.UserDTO;
import com.ecommerce.userservice.payload.response.ExceptionInResponse;
import com.ecommerce.userservice.payload.response.UserDTOResponse;
import com.ecommerce.userservice.security.CustomUserDetails;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(
    name = "User Service",
    description =
        "Authorized users should use this service to get user's details only, "
            + "and Admins should use this service to get, update and delete client details")
@RequiredArgsConstructor
@Validated
public class UserServiceController {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceController.class);

  private final UserService userService;

  private final UserModelAssembler userModelAssembler;

  private final ModelMapper modelMapper;

  /**
   * This API is used to get the details of the user by username
   *
   * @param emailId Get the details of the user by emailID
   * @return user if exists with status code
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
   * This API is used to get list of users
   *
   * @return - embedded models based response with status code
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
   * This API is used to update the user with the values provided on the payload
   *
   * @param updateUserDTO get fields that needs to be updated
   * @return updatedUser and status code
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
        this.userService.updateUser(updateUserDTO, updateUserDTO.getEmailID());
    EntityModel<UserDTOResponse> entityModel = this.userModelAssembler.toModel(updatedUser);
    LOGGER.info("{}", "***** User updated successfully *****");
    return new ResponseEntity<>(entityModel, HttpStatusCode.valueOf(200));
  }

  /**
   * This API is used to update only role by ADMIN
   *
   * @param username to fetch user
   * @return updated user role and status code
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
   * This API is only accessible by ADMIN
   *
   * <p>USER cannot access it
   *
   * @param emailId to check if user exists in the database
   * @return success message and status code
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
   * This API is used to upload the user display picture
   *
   * @param multipartFile imageFile
   * @return response object and status code
   * @throws IOException if any exception occurs
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
    return new ResponseEntity<>("Image uploaded successfully!", HttpStatus.OK);
  }

  /**
   * This API is used to download the image file
   *
   * @param avatarName to make imageName unique
   * @return file and status code
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
   * This API is used to download users data in an Excel file
   *
   * @param response to set content type and headed value
   * @throws IOException to handle Input/Output exceptions
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
      @Schema(hidden = true) @RequestHeader(name = "userRole") String role)
      throws IOException {

    List<UserDTOResponse> listUsers = this.userService.listAll();
    UserExcelExporter exporter = new UserExcelExporter();
    exporter.export(listUsers, response, role);
  }

  /**
   * This API is used to download users data in a Pdf file
   *
   * @param response to set content type and headed value
   * @throws IOException to handle Input/Output exceptions
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
      @Schema(hidden = true) @RequestHeader(name = "userRole") String role)
      throws IOException {

    List<UserDTOResponse> listUsers = this.userService.listAll();
    UserPdfExporter exporter = new UserPdfExporter();
    exporter.export(listUsers, response, role);
  }

  /**
   * @return ROLE_USER if user's role is ROLE_USER else ROLE_ADMIN
   */
  @Operation(
      security = @SecurityRequirement(name = "JWT_token"),
      summary = "Get current loggedIn user's role " + "accessible by all users",
      description = "A GET request to get user's role",
      tags = {"User Service"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully found loggedIn user's " + "role'",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class))),
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
  @GetMapping(value = "/v1/current/role")
  public ResponseEntity<String> getCurrentUserRole(
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    Role originalRole = userDetails.getUser().getRole();
    return new ResponseEntity<>(originalRole.name(), HttpStatus.OK);
  }

  /**
   * This method is used to display the page details on response
   *
   * @param users the list of users
   * @param page the page
   * @return the page details
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
