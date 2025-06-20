package com.ecommerce.payment.payload.response;

import com.ecommerce.payment.payload.request.OrderItemRequest;
import com.ecommerce.payment.payload.request.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import java.util.List;
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
  private List<OrderItemRequest> orderItems;
  private String username;
  private Integer productId;
  private PaymentStatus paymentStatus;
  private LocalDateTime orderDate;
  private LocalDateTime cancelDate;
}
