package com.ecommerce.orderservice.payload.request.order;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderItemRequest {

  private Integer productId;
  private String productSize;
  private Integer quantity;

  public static String toJsonObject(OrderItemRequest orderItemRequest) {
    return JsonObject.mapFrom(orderItemRequest).encodePrettily();
  }
}
