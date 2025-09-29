package com.ecommerce.apigateway.controller;

import com.ecommerce.apigateway.payload.request.ForgotPasswordDTO;
import com.ecommerce.apigateway.payload.request.LoginDTO;
import com.ecommerce.apigateway.payload.request.UserDTO;
import com.ecommerce.apigateway.payload.response.ExceptionInResponse;
import com.ecommerce.apigateway.payload.response.ForgotPasswordResponse;
import com.ecommerce.apigateway.payload.response.UserDTOResponse;
import com.ecommerce.apigateway.security.jwt.TokenHelper;
import com.ecommerce.apigateway.service.AuthenticationService;
import com.ecommerce.apigateway.service.UserService;
import com.ecommerce.apigateway.util.TokenUtil;
import com.ecommerce.apigateway.util.UserModelAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Controller responsible for user-authentication-related operations such as registration, login,
 * and password reset.
 *
 * <p>This class contains endpoints that are used to perform authentication functionalities for
 * users including new user registration, user login to generate JWT tokens, and password
 * reset/update.
 */
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(
    name = "Authentication Service",
    description =
        "All the users should use this service for authentication"
            + " "
            + "also the users can update/reset their passwords")
public class AuthenticationController {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

  private final TokenHelper tokenHelper;

  private final AuthenticationService authenticationService;

  private final UserService userService;

  private final ReactiveAuthenticationManager authenticationManager;

  private final UserModelAssembler userModelAssembler;

  /**
   * Registers a new user in the system and sends a confirmation email upon successful registration.
   *
   * @param userDTO the user data transfer object containing the user's details for registration
   * @return a ResponseEntity containing an EntityModel of the registered user's details if
   *     successful
   * @throws InterruptedException if the operation is interrupted during email sending or processing
   */
  @Operation(
      summary = "Register a new user to be managed by the admins",
      description = "A POST request to register" + " users accessible by <b>NEW USERS</b>",
      tags = {"Authentication Service"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registration successful",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserDTOResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Input validation failed",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class))),
        @ApiResponse(
            responseCode = "409",
            description = "Duplicate emailId",
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
  @PostMapping("/register")
  private ResponseEntity<EntityModel<UserDTOResponse>> registerUser(
      @Valid @RequestBody UserDTO userDTO) throws InterruptedException {

    UserDTOResponse registeredUser = this.authenticationService.createUser(userDTO);
    EntityModel<UserDTOResponse> userModel = this.userModelAssembler.toModel(registeredUser);
    String fullName = userDTO.getFirstName() + " " + userDTO.getLastName();
    this.authenticationService.sendMail(
        userDTO.getEmailId(), fullName, "User Registration Success 🎉");
    return new ResponseEntity<>(userModel, HttpStatus.CREATED);
  }

  /**
   * Retrieves a user's details by their email ID.
   *
   * @param emailId the email ID of the user to be retrieved
   * @return a {@code UserDTOResponse} object containing the user's details
   */
  public UserDTOResponse getUserByEmail(String emailId) {

    UserDTOResponse userDTOResponse = this.userService.getUserByUsername(emailId);
    return userDTOResponse;
  }

  /**
   * Authenticates a user and generates a token for future requests.
   *
   * @param loginDTO The login credentials that include the username and password.
   * @return A Mono containing the ResponseEntity with a JSON object including the generated
   *     authentication token upon successful login. The response may vary based on authentication
   *     results: - On success: Returns a 200 status with the login token. - On failure due to
   *     unregistered user: Returns a 404 status. - On unauthorized access: Returns a 401 status. -
   *     On input validation failure: Returns a 400 status. - On server error: Returns a 500 status.
   */
  @Operation(
      summary = "Login to get a token for authentication",
      description =
          "A POST request to authenticate " + "users accessible by <b>REGISTERED USERS</b>",
      tags = {"Authentication " + "Service"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class))),
        @ApiResponse(
            responseCode = "404",
            description = "User is not registered",
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
            responseCode = "400",
            description = "Input validation failed",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = MethodArgumentNotValidException.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Some " + "Exception Occurred",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionInResponse.class)))
      })
  @PostMapping("/login")
  public Mono<ResponseEntity<JSONObject>> userLogin(@Valid @RequestBody LoginDTO loginDTO) {

    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());

    return authenticationManager
        .authenticate(authenticationToken)
        .flatMap(
            auth -> {
              String token = tokenHelper.generateToken((UserDetails) auth.getPrincipal());
              JSONObject response = TokenUtil.setToken(token);
              LOGGER.info("*** {} ***", "Login Success");

              return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
            });
  }

  /**
   * Handles the forgot password functionality for a registered user.
   *
   * <p>This method processes a PATCH request to reset the user's password and ensures that the
   * updated password is used for future account access.
   *
   * @param forgotPasswordDTO the data transfer object containing user details and the new password
   *     information required for updating the password
   * @return a ResponseEntity containing ForgotPasswordResponse with details of the reset password
   *     operation's result
   */
  @Operation(
      summary = "Reset password for user, and use the updated password for future access",
      description =
          "A " + "PATCH request to update user's password accessible by <b>REGISTERED USERS</b>",
      tags = {"Authentication " + "Service"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully updated user's password'",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ForgotPasswordResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "User not " + "found",
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
  @PatchMapping("/forgot/password")
  public ResponseEntity<ForgotPasswordResponse> forgotPassword(
      @RequestBody ForgotPasswordDTO forgotPasswordDTO) {

    ForgotPasswordResponse forgotPasswordResponse =
        this.authenticationService.forgotPassword(forgotPasswordDTO);
    LOGGER.info("***** Password Updated Successfully *****");
    return ResponseEntity.ok(forgotPasswordResponse);
  }
}
