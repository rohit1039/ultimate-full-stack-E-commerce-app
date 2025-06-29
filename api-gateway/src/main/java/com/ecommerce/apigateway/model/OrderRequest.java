package com.ecommerce.apigateway.model;

import com.ecommerce.apigateway.payload.request.PaymentMethod;
import com.ecommerce.apigateway.payload.request.address.AddressRequest;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class OrderRequest {
  private List<OrderItemRequest> orderItems;
  private AddressRequest address;
}
