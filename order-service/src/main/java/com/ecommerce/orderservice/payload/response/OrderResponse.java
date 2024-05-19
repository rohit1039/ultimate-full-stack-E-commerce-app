package com.ecommerce.orderservice.payload.response;

import static com.ecommerce.orderservice.constant.APIConstants.ORDER_ID;

import com.ecommerce.orderservice.payload.request.order.OrderStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderResponse {

  private String orderId;
  private Long totalQuantity;
  private Long totalAmount;
  private OrderStatus orderStatus;
  private LocalDateTime orderDate;
  private LocalDateTime cancelDate;

  public JsonObject buildOrderResponse(String orderId) {
    return new JsonObject().put(ORDER_ID, orderId);
  }
}
