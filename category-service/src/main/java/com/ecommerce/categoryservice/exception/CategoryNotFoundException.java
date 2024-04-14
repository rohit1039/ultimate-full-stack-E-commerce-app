package com.ecommerce.categoryservice.exception;

public class CategoryNotFoundException extends RuntimeException {

  public CategoryNotFoundException(String message) {

    super(message);
  }
}
