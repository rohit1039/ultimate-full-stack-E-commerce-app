package com.ecommerce.apigateway.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFoundException(
      UserNotFoundException exception) {

    ErrorResponse errorResponse = new ErrorResponse(404, exception.getLocalizedMessage());

    LOGGER.error("*** {} ***", exception.getLocalizedMessage());

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(
      BadCredentialsException exception) {

    ErrorResponse errorResponse = new ErrorResponse(401, exception.getLocalizedMessage());

    LOGGER.error("*** {} ***", exception.getLocalizedMessage());

    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }
}
