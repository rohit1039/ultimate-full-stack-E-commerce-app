package com.ecommerce.orderservice.payload.request.order;

import com.ecommerce.orderservice.payload.request.address.AddressRequest;
import com.ecommerce.orderservice.payload.request.payment.PaymentRequest;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.vertx.core.json.JsonObject;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderRequest {

  @NotNull private List<OrderItemRequest> orderItems;
  private String username;
  @Valid private AddressRequest shippingAddress;
  @Valid private PaymentRequest transactionDetails;

  public static JsonObject toJson(OrderRequest orderRequest) {
    return JsonObject.mapFrom(orderRequest);
  }
}
