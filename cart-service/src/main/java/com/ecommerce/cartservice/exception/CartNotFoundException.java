package com.ecommerce.cartservice.exception;

public class CartNotFoundException extends RuntimeException {

  public CartNotFoundException(String message) {

    super(message);
  }
}
