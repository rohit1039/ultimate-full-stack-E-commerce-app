package com.ecommerce.userservice.exception;

public class UnAuthorizedException extends RuntimeException {

  public UnAuthorizedException(String message) {

    super(message);
  }
}
