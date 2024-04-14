package com.ecommerce.productservice.exception;

public class MissingHeaderException extends RuntimeException {

  public MissingHeaderException(String msg) {

    super(msg);
  }
}
