package com.ecommerce.productservice.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ToString
public class Size implements Serializable {

  @Serial private static final long serialVersionUID = 1759477433483466736L;

  @NotNull
  @Schema(example = "S", description = "The size of the product")
  private String name;

  @NotNull
  @Schema(example = "5", description = "The number of products with this size")
  private Integer quantity;

  @Schema(example = "2", description = "The number of reserved (but unpaid) items")
  private Integer reservedQuantity;
}
