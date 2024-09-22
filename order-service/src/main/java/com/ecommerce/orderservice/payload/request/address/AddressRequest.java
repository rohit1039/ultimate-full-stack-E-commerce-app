package com.ecommerce.orderservice.payload.request.address;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
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
public class AddressRequest {

  private String addressId;
  @NotNull private String firstName;
  @NotNull private String lastName;
  @NotNull private String addressLine1;
  private AddressType addressType;
  private String addressLine2;
  @NotNull private String cityName;
  @NotNull private String stateName;
  @NotNull private Long postalCode;
  @NotNull private Long phoneNumber;
  private String username;

  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  @JsonFormat(pattern = "dd/MM/yyyy hh:mm:ss a")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  private LocalDateTime addressCreationDate;

  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  @JsonFormat(pattern = "dd/MM/yyyy hh:mm:ss a")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  private LocalDateTime lastUpdatedAddressDate;

  public JsonObject toJsonObject(AddressRequest addressRequest) {
    return JsonObject.mapFrom(addressRequest);
  }
}
