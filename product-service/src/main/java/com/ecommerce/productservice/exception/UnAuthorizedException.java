package com.ecommerce.productservice.exception;

public class UnAuthorizedException extends RuntimeException {

  public UnAuthorizedException(String message) {

    super(message);
  }
}
