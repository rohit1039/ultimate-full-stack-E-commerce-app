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
   * This API is used to register the user, and save into the database.
   *
   * @param userDTO receive request from payload
   * @return model based response and status code
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

  public UserDTOResponse getUserByEmail(String emailId) {

    UserDTOResponse userDTOResponse = this.userService.getUserByUsername(emailId);
    return userDTOResponse;
  }

  /**
   * This API is used to log in, so that JWT can be generated
   *
   * @param loginDTO payload to get username and password
   * @return JWTResponse
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
   * This API is used to enable the user update their password by satisfying the password criteria
   *
   * @param forgotPasswordDTO Request to reset password
   * @return either JWTResponse or error Message with status codes
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
