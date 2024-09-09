package com.ecommerce.apigateway.exception;

public class MissingHeaderException extends RuntimeException {

  public MissingHeaderException(String msg) {

    super(msg);
  }
}
