package com.ecommerce.apigateway.exception;

import java.util.List;
import lombok.Data;

@Data
public class ClientException extends RuntimeException {

  private final List<ErrorResponse> errors;

  public ClientException(List<ErrorResponse> errors) {
    super("Client exception");
    this.errors = errors;
  }
}
