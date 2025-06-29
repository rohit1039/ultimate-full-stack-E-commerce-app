package com.ecommerce.apigateway.payload.response;

import com.ecommerce.apigateway.model.OrderItemRequest;
import com.ecommerce.apigateway.model.OrderStatus;
import com.ecommerce.apigateway.payload.request.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderResponse {
  private String orderId;
  private Float totalAmount;
  private List<OrderItemRequest> orderItems;
  private String username;
  private PaymentStatus paymentStatus;
  private OrderStatus orderStatus;
  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  @JsonFormat(pattern = "dd/MM/yyyy hh:mm:ss a")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  private LocalDateTime orderDate;
  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  @JsonFormat(pattern = "dd/MM/yyyy hh:mm:ss a")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  private LocalDateTime cancelDate;
}
