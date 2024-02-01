package com.ecommerce.productservice.payload.request;

import com.ecommerce.productservice.model.Size;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ToString
public class ProductRequestDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1759477433483466736L;

	@NotBlank
	@Schema(description = "Product Name", example = "Black T-Shirt")
	private String productName;

	@NotBlank
	@Schema(description = "Product Brand", example = "DENIM")
	private String productBrand;

	@NotBlank
	@Schema(description = "Product Main Image", example = "main.png")
	private String productMainImage;

	@Schema(description = "Product Images", type = "set",
			example = "[\"default1.png\", \"default2.png\",\"default3.png\"]")
	private Set<String> extraProductImages;

	@NotBlank
	@Schema(description = "Short Description",
			example = "A half sleeve solid color polo, with a ribbed collar and zipper closure.")
	private String shortDescription;

	@NotEmpty
	@Schema(description = "Long Description", type = "array", example = "[\"A half sleeve solid color polo\","
			+ " \"with a ribbed collar and zipper closure\"," + " \"cotton Fabric, and slim Fit.\"]")
	private String[] longDescription;

	@NotBlank
	@Schema(description = "Product Color", example = "black")
	private String productColor;

	@NotEmpty
	@Schema(description = "Product Sizes", type = "set",
			example = "[{" + "\"name\": \"M\"," + "\"quantity\": 25" + "}," + "{" + "\"name\": \"L\","
					+ "\"quantity\": 25" + "}," + "{" + "\"name\": \"S\"," + "\"quantity\": 25" + "}," + "{"
					+ "\"name\": \"XL\"," + "\"quantity\": 25" + "}]")
	@Valid
	private Set<Size> productSizes;

	@NotNull
	@Positive
	@Schema(description = "Discount Percent", example = "5")
	private Integer discountPercent;

	@NotNull
	@Positive
	@Schema(description = "Product Price", example = "1500.0")
	private Float productPrice;

}
