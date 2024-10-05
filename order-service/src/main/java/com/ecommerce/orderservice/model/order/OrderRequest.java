package com.ecommerce.orderservice.model.order;

import com.ecommerce.orderservice.model.address.AddressRequest;
import com.ecommerce.orderservice.model.payment.PaymentRequest;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.vertx.core.json.JsonObject;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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

  @NotNull
  private List<OrderItemRequest> orderItems;
  private String username;
  @Valid
  private AddressRequest shippingAddress;
  @Valid
  private PaymentRequest transactionDetails;

  public static JsonObject toJson(OrderRequest orderRequest) {

    return JsonObject.mapFrom(orderRequest);
  }
}
