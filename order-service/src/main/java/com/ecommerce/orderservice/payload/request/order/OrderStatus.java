package com.ecommerce.orderservice.payload.request.order;

public enum OrderStatus {
  PENDING, CONFIRMED, DISPATCHED, SHIPPED, DELIVERED, CANCELLED, DECLINED, REFUNDED,
  AWAITING_PAYMENT, AWAITING_SHIPMENT
}
