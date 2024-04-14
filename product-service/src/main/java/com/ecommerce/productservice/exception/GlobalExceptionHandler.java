package com.ecommerce.productservice.exception;

import com.ecommerce.productservice.payload.response.ExceptionInResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  /** The class logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
    LOGGER.error("*** {} ***", exceptionInResponse.getErrorMessage());
    return new ResponseEntity<>(exceptionInResponse, HttpStatus.UNAUTHORIZED);
  }

  /**
   * Handles the ProductNotFoundException exception.
   *
   * @param exception the exception
   * @return the response entity
   */
  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ExceptionInResponse> handleProductNotFoundException(
      ProductNotFoundException exception) {

    String errorDescription;
    if (exception.getMessage().contains("categoryId")) {
      errorDescription = "Please try with different category Id";
    } else if (exception.getMessage().contains("No products found")) {
      errorDescription = "No products exists";
    } else {
      errorDescription = "Please try with different product Id";
    }
    ExceptionInResponse exceptionInResponseForProduct =
        new ExceptionInResponse(404, exception.getMessage(), errorDescription);
    LOGGER.error("*** {} ***", exceptionInResponseForProduct.getErrorMessage());
    return new ResponseEntity<>(
        exceptionInResponseForProduct,
        HttpStatusCode.valueOf(exceptionInResponseForProduct.getErrorCode()));
  }

  /**
   * Handles the DuplicateProductException exception.
   *
   * @param duplicateProductException the exception
   * @return the response entity
   */
  @ExceptionHandler(DuplicateProductException.class)
  public ResponseEntity<ExceptionInResponse> handleDuplicateProductException(
      DuplicateProductException duplicateProductException) {

    ExceptionInResponse exceptionInResponse =
        new ExceptionInResponse(
            409,
            duplicateProductException.getMessage(),
            "Please try " + "with different product name");
    LOGGER.error("*** {} ***", exceptionInResponse.getErrorDescription());
    return new ResponseEntity<>(
        exceptionInResponse, HttpStatusCode.valueOf(exceptionInResponse.getErrorCode()));
  }

  /**
   * Handles the Exception.
   *
   * @param exception the exception
   * @return the response entity
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ExceptionInResponse> handleGenericException(Exception exception) {

    ExceptionInResponse exceptionInResponse =
        new ExceptionInResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "INTERNAL_SERVER_ERROR",
            exception.getMessage());
    LOGGER.error("*** {} ***", exceptionInResponse.getErrorDescription());
    return new ResponseEntity<>(exceptionInResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Handles the ClientException exception.
   *
   * @param errorFromClient the exception
   * @return the response entity
   */
  @ExceptionHandler(ClientException.class)
  public ResponseEntity<String> handleClientException(ClientException errorFromClient) {

    LOGGER.error("*** Client error: {} ***", errorFromClient.getErrorMessage());
    return new ResponseEntity<>(
        errorFromClient.getErrorMessage(), HttpStatusCode.valueOf(errorFromClient.getErrorCode()));
  }

  /**
   * This function is used to handle the MethodArgumentNotValidException exception. It is a
   * Spring-specific exception that is thrown when the request parameters do not match the method
   * arguments. This function iterates through the field and global errors of the
   * MethodArgumentNotValidException and adds them to a map, where the key is the field name and the
   * value is the error message. It then returns a response with the map as the body and a status
   * code of BAD_REQUEST.
   *
   * @param ex The MethodArgumentNotValidException exception that was thrown
   * @param headers The headers of the request
   * @param status The status code of the response
   * @param request The web request
   * @return A response with the error messages in a map as the body and a status code of
   *     BAD_REQUEST
   */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    final Map<String, String> errors = new ConcurrentHashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(
            (error) -> {
              errors.put(error.getField(), error.getDefaultMessage());
            });
    ex.getBindingResult()
        .getGlobalErrors()
        .forEach(
            (errorGlobal) -> {
              errors.put(((FieldError) errorGlobal).getField(), errorGlobal.getDefaultMessage());
            });
    LOGGER.error("{}", errors);
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }
}
