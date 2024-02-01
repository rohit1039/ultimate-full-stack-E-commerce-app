package com.ecommerce.productservice.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ToString
public class Size implements Serializable {

	@Serial
	private static final long serialVersionUID = 1759477433483466736L;

	@NotNull
	@Schema(example = "S", description = "The size of the product")
	private String name;

	@NotNull
	@Schema(example = "5", description = "The number of products with this size")
	private Integer quantity;

}
