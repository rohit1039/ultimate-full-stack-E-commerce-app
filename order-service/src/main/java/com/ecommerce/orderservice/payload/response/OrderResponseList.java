package com.ecommerce.orderservice.payload.response;

import com.ecommerce.orderservice.payload.request.address.AddressRequest;
import com.ecommerce.orderservice.payload.request.order.OrderStatus;
import com.ecommerce.orderservice.payload.request.payment.PaymentRequest;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderResponseList {
  private String orderId;
  @NotNull private JsonArray orderItems;
  private String username;
  private List<ProductResponse> products;
  @Valid private AddressRequest shippingAddress;
  @Valid private PaymentRequest transactionDetails;

  public static List<JsonObject> toJsonList(List<OrderResponseList> orderResponseList) {
    List<JsonObject> jsonObjectList = new ArrayList<>();
    for (OrderResponseList responseList : orderResponseList) {
      jsonObjectList.add(JsonObject.mapFrom(responseList));
    }
    return jsonObjectList;
  }
}
