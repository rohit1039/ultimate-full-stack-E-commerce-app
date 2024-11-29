package com.ecommerce.orderservice.payload.response;

import com.ecommerce.orderservice.payload.request.order.OrderStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
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
public class OrderResponseList {

  private String orderId;
  private OrderStatus orderStatus;
  private JsonArray orderItems;
  private String username;
  private List<ProductResponse> products;

  public static List<JsonObject> toJsonList(List<OrderResponseList> orderResponseList) {

    List<JsonObject> jsonObjectList = new ArrayList<>();
    for (OrderResponseList responseList : orderResponseList) {
      jsonObjectList.add(JsonObject.mapFrom(responseList));
    }
    return jsonObjectList;
  }
}
