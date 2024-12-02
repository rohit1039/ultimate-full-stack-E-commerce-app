package com.ecommerce.orderservice.payload.request.order;

public enum OrderStatus {
  PENDING, CONFIRMED, DISPATCHED, SHIPPED, DELIVERED, CANCELLED, DECLINED, REFUNDED,
  AWAITING_PAYMENT, AWAITING_SHIPMENT;

  public static boolean isValid(String value) {

    for (OrderStatus status : OrderStatus.values()) {
      if (status.name().equalsIgnoreCase(value)) {
        return true;
      }
    }
    return false;
  }
}
