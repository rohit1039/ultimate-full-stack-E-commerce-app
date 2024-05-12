package com.ecommerce.orderservice.payload.response;

import static com.ecommerce.orderservice.constant.APIConstants.ORDER_ID;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.vertx.core.json.JsonObject;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderResponse {
  private String orderId;

  public JsonObject buildOrderResponse(String orderId) {
    return new JsonObject().put(ORDER_ID, orderId);
  }
}
