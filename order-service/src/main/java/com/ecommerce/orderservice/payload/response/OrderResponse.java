package com.ecommerce.orderservice.payload.response;

import com.ecommerce.orderservice.payload.request.order.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {

  private String orderId;
  private Long totalQuantity;
  private Long totalAmount;
  private String username;
  private Integer productId;
  private OrderStatus orderStatus;
  private LocalDateTime orderDate;
  private LocalDateTime cancelDate;
}
