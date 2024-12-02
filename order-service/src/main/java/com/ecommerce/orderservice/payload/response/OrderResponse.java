package com.ecommerce.orderservice.payload.response;

import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_ID;

import com.ecommerce.orderservice.payload.request.order.OrderStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderResponse {

  @JsonProperty("_id")
  private String orderId;
  private Long totalQuantity;
  private Long totalAmount;
  private String username;
  private Integer productId;
  private OrderStatus orderStatus;
  private LocalDateTime orderDate;
  private LocalDateTime cancelDate;

  public JsonObject buildOrderResponse(String orderId) {

    return new JsonObject().put(ORDER_ID, orderId);
  }
}
