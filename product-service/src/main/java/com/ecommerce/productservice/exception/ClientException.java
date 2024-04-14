package com.ecommerce.productservice.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ClientException extends RuntimeException {

  private int errorCode;

  private String errorMessage;

  private String errorDescription;

  public ClientException(int errorCode, String errorMessage, String errorDescription) {

    super(errorMessage);
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.errorDescription = errorDescription;
  }
}
