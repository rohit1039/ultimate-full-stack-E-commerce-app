package com.ecommerce.userservice.exception;

import com.ecommerce.userservice.payload.response.ExceptionInResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ExceptionInResponse> handleGenericException(Exception exception) {

    ExceptionInResponse exceptionInResponse =
        new ExceptionInResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.toString(),
            exception.getMessage(),
            "Some error occurred");
    LOGGER.error("*** {} ***", exceptionInResponse.getErrorMessage());
    return new ResponseEntity<>(exceptionInResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(DuplicateUsernameException.class)
  public ResponseEntity<ExceptionInResponse> handleDuplicateUsernameException(
      DuplicateUsernameException exception) {

    ExceptionInResponse exceptionInResponse =
        new ExceptionInResponse(
            "USER_ALREADY_EXISTS",
            exception.getLocalizedMessage(),
            "Please try with different emailId");
    LOGGER.error("*** {} ***", exceptionInResponse.getErrorMessage());
    return new ResponseEntity<>(exceptionInResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(UnAuthorizedException.class)
  public ResponseEntity<ExceptionInResponse> handleUnAuthorizedException(
      UnAuthorizedException exception) {

    ExceptionInResponse exceptionInResponse =
        new ExceptionInResponse(
            "UNAUTHORIZED_ACCESS", exception.getLocalizedMessage(), "Role should be ROLE_ADMIN");
    LOGGER.error("*** {} ***", exceptionInResponse.getErrorMessage());
    return new ResponseEntity<>(exceptionInResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ExceptionInResponse> handleUserNotFoundException(
      UserNotFoundException userNotFoundException) {

    ExceptionInResponse exceptionInResponse =
        new ExceptionInResponse(
            "USER_NOT_FOUND",
            userNotFoundException.getMessage(),
            "Please try with correct username");
    LOGGER.error("*** {} ***", exceptionInResponse.getErrorMessage());
    return new ResponseEntity<>(exceptionInResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(MissingHeaderException.class)
  protected ResponseEntity<ExceptionInResponse> handleMissingRequestHeaderException(
      MissingHeaderException ex) {

    ExceptionInResponse exceptionInResponse =
        new ExceptionInResponse(
            "MISSING_AUTHORIZATION_HEADER",
            ex.getMessage(),
            "Authentication required. Please authenticate through JWT");
    LOGGER.error("*** {} ***", exceptionInResponse.getErrorMessage());
    return new ResponseEntity<>(exceptionInResponse, HttpStatus.BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {

    final Map<String, String> errors = new ConcurrentHashMap<>();
    exception
        .getBindingResult()
        .getFieldErrors()
        .forEach(
            (error) -> {
              errors.put(error.getField(), error.getDefaultMessage());
            });
    exception
        .getBindingResult()
        .getGlobalErrors()
        .forEach(
            (errorGlobal) -> {
              errors.put(((FieldError) errorGlobal).getField(), errorGlobal.getDefaultMessage());
            });
    LOGGER.error("*** {} ***", errors);
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }
}
