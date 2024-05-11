package com.ecommerce.orderservice.payload.request;

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
public class OrderRequest {

  private String fullName;

  private Long contactNumber;

  private String addressLine1Txt;

  private String addressLine2Txt;

  private String landMark;

  private String countryCode;

  private String regionCode;

  private String pinCode;

  private String cityName;

  public JsonObject toJsonObject(OrderRequest orderRequest) {
    return JsonObject.mapFrom(orderRequest);
  }
}
