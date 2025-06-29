package com.ecommerce.apigateway.exception;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * The GlobalExceptionHandler is a centralized exception handler that intercepts and processes
 * exceptions thrown across the entire application. It uses the Spring @RestControllerAdvice
 * annotation to provide global handling of exceptions while keeping controllers clean and focused.
 *
 * <p>This class defines specific exception handlers for custom and framework-specific exceptions to
 * provide appropriate error responses and log error details.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * Handles exceptions of type UserNotFoundException and generates an appropriate error response.
   *
   * <p>This method is triggered whenever a UserNotFoundException is thrown in the application. It
   * creates an ErrorResponse containing the error code and localized message and returns it with a
   * 404 (NOT FOUND) HTTP status code.
   *
   * @param exception the UserNotFoundException that was thrown
   * @return ResponseEntity containing an ErrorResponse with details about the exception
   */
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFoundException(
      UserNotFoundException exception) {

    ErrorResponse errorResponse =
        new ErrorResponse(exception.getMessage(), "User doesn't exist in database");

    LOGGER.error("*** {} ***", exception.getLocalizedMessage());

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  /**
   * Handles the BadCredentialsException, which is thrown when authentication fails due to invalid
   * credentials. Constructs an appropriate error response with an HTTP status code of 401
   * (Unauthorized) and logs the exception message.
   *
   * @param exception the BadCredentialsException instance that was thrown
   * @return a ResponseEntity containing the ErrorResponse object and an HTTP status of UNAUTHORIZED
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(
      BadCredentialsException exception) {

    ErrorResponse errorResponse =
        new ErrorResponse(exception.getMessage(), "Wrong username or password");

    LOGGER.error("*** {} ***", exception.getLocalizedMessage());

    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  /**
   * Handles exceptions of type ClientException and generates an appropriate error response. This
   * method is invoked automatically when a ClientException occurs in the application.
   *
   * @param ex the ClientException instance that was thrown
   * @return a ResponseEntity containing a map with the error details and an HTTP status of BAD
   *     REQUEST
   */
  @ExceptionHandler(ClientException.class)
  public ResponseEntity<List<ErrorResponse>> handleClientError(ClientException ex) {

    LOGGER.error("*** {} ***", ex.getErrors());

    return ResponseEntity.badRequest().body(ex.getErrors());
  }

  /**
   * Handles exceptions of type {@code RuntimeException} thrown in the application. Generates an
   * appropriate error response with an HTTP status code of 500 (Internal Server Error). Includes
   * the error message from the exception in the response body.
   *
   * @param ex the {@code RuntimeException} instance that was thrown
   * @return a {@code ResponseEntity} with an HTTP status of {@code INTERNAL_SERVER_ERROR} and a
   *     body containing the error message
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<?> handleServerError(RuntimeException ex) {

    ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), "Some error occurred");

    LOGGER.error("*** {} ***", ex.getLocalizedMessage());

    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
