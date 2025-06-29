package com.ecommerce.apigateway.model;

public enum OrderStatus {
  PENDING,
  CONFIRMED,
  PAYMENT_FAILED,
  AWAITING_PAYMENT,
  AWAITING_SHIPMENT;

  public static boolean isValid(String value) {

    for (OrderStatus status : OrderStatus.values()) {
      if (status.name().equalsIgnoreCase(value)) {
        return true;
      }
    }
    return false;
  }
}
