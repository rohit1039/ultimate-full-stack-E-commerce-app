package com.ecommerce.categoryservice.exception;

import com.ecommerce.categoryservice.payload.response.ExceptionInResponse;
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

/**
 * This class handles all the exceptions that occur in the application. It extends the
 * ResponseEntityExceptionHandler class, which provides default exception handling for Spring Web
 * MVC controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * This method handles CategoryNotFoundException exceptions. It returns a ResponseEntity with a
   * status code of NOT_FOUND and an ExceptionInResponse object that contains the error message.
   *
   * @param categoryNotFoundException the CategoryNotFoundException exception that was thrown
   * @return a ResponseEntity with a status code of NOT_FOUND and an ExceptionInResponse object
   */
  @ExceptionHandler(CategoryNotFoundException.class)
  public ResponseEntity<ExceptionInResponse> handleUserNotFoundException(
      CategoryNotFoundException categoryNotFoundException) {

    ExceptionInResponse exceptionInResponse =
        new ExceptionInResponse(
            HttpStatus.NOT_FOUND.value(),
            "NO_CATEGORIES_FOUND",
            categoryNotFoundException.getLocalizedMessage());
    return new ResponseEntity<>(exceptionInResponse, HttpStatus.NOT_FOUND);
  }

  /**
   * This method handles CategoryDuplicationException exceptions. It returns a ResponseEntity with a
   * status code of CONFLICT and an ExceptionInResponse object that contains the error message.
   *
   * @param categoryDuplicationException the CategoryDuplicationException exception that was thrown
   * @return a ResponseEntity with a status code of CONFLICT and an ExceptionInResponse object
   */
  @ExceptionHandler(CategoryDuplicationException.class)
  public ResponseEntity<ExceptionInResponse> handleCategoryDuplicateException(
      CategoryDuplicationException categoryDuplicationException) {

    ExceptionInResponse exceptionInResponse =
        new ExceptionInResponse(
            HttpStatus.CONFLICT.value(),
            "CATEGORY_DUPLICATION_NOT_ALLOWED",
            categoryDuplicationException.getLocalizedMessage());
    return new ResponseEntity<>(exceptionInResponse, HttpStatus.CONFLICT);
  }

  /**
   * This method handles UnAuthorizedException exceptions. It returns a ResponseEntity with a status
   * code of UNAUTHORIZED and an ExceptionInResponse object that contains the error message.
   *
   * @param unAuthorizedException the UnAuthorizedException exception that was thrown
   * @return a ResponseEntity with a status code of UNAUTHORIZED and an ExceptionInResponse object
   */
  @ExceptionHandler(UnAuthorizedException.class)
  public ResponseEntity<ExceptionInResponse> handleUnAuthorizedException(
      UnAuthorizedException unAuthorizedException) {

    ExceptionInResponse exceptionInResponse =
        new ExceptionInResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "UNAUTHORIZED_ACCESS",
            unAuthorizedException.getLocalizedMessage());
    return new ResponseEntity<>(exceptionInResponse, HttpStatus.UNAUTHORIZED);
  }

  /**
   * This method handles all other exceptions. It returns a ResponseEntity with a status code of
   * INTERNAL_SERVER_ERROR and an ExceptionInResponse object that contains the error message.
   *
   * @param exception the exception that was thrown
   * @return a ResponseEntity with a status code of INTERNAL_SERVER_ERROR and an ExceptionInResponse
   *     object
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ExceptionInResponse> handleGenericException(Exception exception) {

    ExceptionInResponse exceptionInResponse =
        new ExceptionInResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "INTERNAL_SERVER_ERROR",
            exception.getLocalizedMessage());
    return new ResponseEntity<>(exceptionInResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * This method handles MissingHeaderException exceptions. It returns a ResponseEntity with a
   * status code of BAD_REQUEST and an ExceptionInResponse object that contains the error message.
   *
   * @param ex the MissingHeaderException exception that was thrown
   * @return a ResponseEntity with a status code of BAD_REQUEST and an ExceptionInResponse object
   */
  @ExceptionHandler(MissingHeaderException.class)
  protected ResponseEntity<ExceptionInResponse> handleMissingRequestHeaderException(
      MissingHeaderException ex) {

    ExceptionInResponse exceptionInResponse =
        new ExceptionInResponse(
            HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), "Please authorize through JWT");
    LOGGER.error("*** {} ***", exceptionInResponse.getErrorMessage());
    return new ResponseEntity<>(exceptionInResponse, HttpStatus.UNAUTHORIZED);
  }

  /**
   * This method handles ClientException exceptions. It returns a ResponseEntity with the error
   * message from the ClientException object.
   *
   * @param errorFromClient the ClientException exception that was thrown
   * @return a ResponseEntity with the error message from the ClientException object
   */
  @ExceptionHandler(ClientException.class)
  public ResponseEntity<String> handleClientException(ClientException errorFromClient) {

    LOGGER.error("*** {} ***", errorFromClient.getErrorMessage());
    return new ResponseEntity<>(
        errorFromClient.getErrorMessage(), HttpStatusCode.valueOf(errorFromClient.getErrorCode()));
  }

  /**
   * This method handles MethodArgumentNotValidException exceptions. It returns a ResponseEntity
   * with a status code of BAD_REQUEST and a map of error messages for each field that is invalid.
   *
   * @param exception the MethodArgumentNotValidException exception that was thrown
   * @param headers the HTTP headers
   * @param status the HTTP status code
   * @param request the current web request
   * @return a ResponseEntity with a status code of BAD_REQUEST and a map of error messages for each
   *     field that is invalid
   */
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
    LOGGER.error("{}", errors);
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }
}
