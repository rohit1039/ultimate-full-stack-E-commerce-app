package com.ecommerce.productservice.payload.response;

import com.ecommerce.productservice.model.Size;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.core.Relation;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "products", itemRelation = "product")
@JsonPropertyOrder({ "categoryId", "productId", "productName", "productBrand", "username", "productMainImage",
		"extraProductImages", "shortDescription", "longDescription", "createdAt", "updatedAt", "productPrice",
		"productSizes", "productCount", "productColor", "discountPercent", "reviewCount", "averageRating", "enabled",
		"inStock" })
public class ProductResponseDTO extends CollectionModel<ProductResponseDTO> implements Serializable {

	@Serial
	private static final long serialVersionUID = 1759477433483466736L;

	private Integer productId;

	private String productName;

	private String productBrand;

	private String productMainImage;

	private Set<String> extraProductImages;

	private Integer categoryId;

	private String shortDescription;

	private String[] longDescription;

	private String productColor;

	private Set<Size> productSizes;

	private Integer discountPercent;

	private Float discountedPrice;

	private Float totalPrice;

	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	@JsonFormat(pattern = "dd/MM/yyyy hh:mm:ss a")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime createdAt;

	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	@JsonFormat(pattern = "dd/MM/yyyy hh:mm:ss a")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime updatedAt;

	private Float productPrice;

	private Long productCount;

	private Integer reviewCount;

	private Float averageRating;

	private boolean enabled;

	private boolean inStock;

	@CreatedBy
	@LastModifiedBy
	private String username;

	public String getShortDescription() {

		return shortDescription.length() > 50 ? shortDescription.substring(0, 50).concat("...") : shortDescription;
	}

}
