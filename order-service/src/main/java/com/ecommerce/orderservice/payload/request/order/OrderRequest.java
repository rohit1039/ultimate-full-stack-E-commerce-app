package com.ecommerce.orderservice.payload.request.order;

import com.ecommerce.orderservice.payload.request.address.AddressRequest;
import com.ecommerce.orderservice.payload.request.payment.PaymentRequest;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderRequest {

  private Long userId;
  private Long productId;
  private Long totalDiscount;
  private Long totalAmount;
  private OrderStatus orderStatus;
  private PaymentRequest paymentRequest;
  private AddressRequest addressRequest;
  private LocalDateTime orderDate;
  private LocalDateTime cancelDate;
  private LocalDateTime deliveryDate;
  private LocalDateTime dispatchOrderDate;
  private LocalDateTime refundDate;

  public JsonObject toJsonObject(OrderRequest orderRequest) {
    return JsonObject.mapFrom(orderRequest);
  }
}
