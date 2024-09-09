package com.ecommerce.apigateway.exception;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(String message) {

    super(message);
  }
}
