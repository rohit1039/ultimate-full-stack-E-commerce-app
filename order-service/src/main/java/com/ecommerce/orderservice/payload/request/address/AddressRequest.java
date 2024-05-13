package com.ecommerce.orderservice.payload.request.address;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AddressRequest {

  private String firstName;
  private String lastName;
  private String addressLine1;
  private AddressType addressType;
  private String addressLine2;
  private String cityName;
  private String stateName;
  private Long postalCode;
  private Long phoneNumber;
  private LocalDateTime addressCreationDate;
  private LocalDateTime lastUpdatedAddressDate;

  public JsonObject toJsonObject(AddressRequest addressRequest) {
    return JsonObject.mapFrom(addressRequest);
  }
}
