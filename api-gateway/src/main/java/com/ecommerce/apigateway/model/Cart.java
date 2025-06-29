package com.ecommerce.apigateway.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Cart {

  @MongoId private Long id;

  @Field
  @Schema(description = "username", example = "testuser@gmail.com")
  private String username;

  @Schema(
      description = "Items to be added in the cart",
      type = "array",
      example =
          "["
              + "{\"productId\": 101, \"size\": \"L\", \"quantity\": 1},"
              + " {\"productId\": 202, \"size\": \"M\", \"quantity\": 1}"
              + "]")
  private List<CartItem> items;
}
