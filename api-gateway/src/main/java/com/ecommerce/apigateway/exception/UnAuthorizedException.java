package com.ecommerce.apigateway.exception;

public class UnAuthorizedException extends RuntimeException {

  public UnAuthorizedException(String message) {

    super(message);
  }
}
