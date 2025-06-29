package com.ecommerce.apigateway.payload.request.address;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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

  @NotNull
  @Schema(example = "Test")
  private String firstName;

  @NotNull
  @Schema(example = "User")
  private String lastName;

  @NotNull
  @Schema(example = "Plot 5, Tech Park")
  private String addressLine1;

  @Schema(example = "HOME")
  private AddressType addressType;

  @Schema(example = "Near InfoCity")
  private String addressLine2;

  @NotNull
  @Schema(example = "Bhubaneswar")
  private String cityName;

  @Schema(example = "Kendujhar")
  private String district;

  @NotNull
  @Schema(example = "Odisha")
  private String stateName;

  @NotNull
  @Schema(example = "758015")
  private Long postalCode;

  @NotNull @JsonIgnore private String phoneNumber;
  @JsonIgnore private String username;

  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  @JsonFormat(pattern = "dd/MM/yyyy hh:mm:ss a")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonIgnore
  private LocalDateTime addressCreationDate;

  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  @JsonFormat(pattern = "dd/MM/yyyy hh:mm:ss a")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonIgnore
  private LocalDateTime lastUpdatedAddressDate;
}
