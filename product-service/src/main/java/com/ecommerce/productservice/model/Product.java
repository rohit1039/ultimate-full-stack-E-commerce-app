package com.ecommerce.productservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "products")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class Product implements Serializable {

	@Serial
	private static final long serialVersionUID = 1759477433483466736L;

	@Transient
	public static final String SEQUENCE_NAME = "products_sequence";

	@Indexed(unique = true)
	@MongoId
	private Integer productId;

	@Field("product_name")
	private String productName;

	@Field("product_brand")
	private String productBrand;

	@Field("product_main_image")
	private String productMainImage;

	@Field("product_images")
	private Set<String> extraProductImages;

	@Field("category_id")
	private Integer categoryId;

	@Field("short_desc")
	private String shortDescription;

	@Field("long_desc")
	private String[] longDescription;

	@Field("product_color")
	private String productColor;

	@Field("product_sizes")
	private Set<Size> productSizes = new HashSet<>();

	@Field("discount_percent")
	private Integer discountPercent;

	@Field("discount_price")
	private Float discountedPrice;

	@Field("total_price")
	private Float totalPrice;

	@Field("product_price")
	private Float productPrice;

	@Field("product_count")
	private Integer productCount; // total number of products available

	@Field("created_at")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime createdAt;

	@Field("updated_at")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime updatedAt;

	@Transient
	private String username;

	@Field("review_count")
	private Integer reviewCount;

	@Field("average_rating")
	private Float averageRating;

	@Field("is_enabled")
	private boolean enabled;

	@Field("in_stock")
	private boolean inStock;

	public boolean isInStock() {

		return productCount > 0;
	}

	public Integer getProductCount() {

		return Math.toIntExact(productSizes.stream()
			.map(Size::getQuantity)
			.collect(Collectors.summarizingInt(Integer::intValue))
			.getSum());
	}

	public Float getDiscountedPrice() {

		return productPrice * discountPercent / 100;
	}

	public Float getTotalPrice() {

		return productPrice - getDiscountedPrice();
	}

}
