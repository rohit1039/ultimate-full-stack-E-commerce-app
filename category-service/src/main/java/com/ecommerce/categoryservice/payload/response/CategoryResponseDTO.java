package com.ecommerce.categoryservice.payload.response;

import com.ecommerce.categoryservice.model.Category;
import com.ecommerce.categoryservice.util.CategoryFilter;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonFilter(value = "filter_parent")
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonPropertyOrder({ "categoryId", "categoryName", "categoryImage", "enabled", "createdBy", "updatedBy", "hasChildren",
		"createdAt", "updatedAt", "parent" })
@Relation(collectionRelation = "categories", itemRelation = "category")
public class CategoryResponseDTO {

	private Long categoryId;

	private String categoryName;

	private boolean enabled;

	private String createdBy;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String updatedBy;

	/**
	 * We have parent category, so for parent categories there will be no parent present
	 * in this case the parent will be null. So when parent is null we should exclude it
	 * from our JSON response
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Category parent;

	/**
	 * Basically it will not display the hasChildren field when it is false, will display
	 * only if it is true
	 */
	@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = CategoryFilter.class)
	private boolean hasChildren;

	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	@JsonFormat(pattern = "dd/MM/yyyy hh:mm:ss a")
	private LocalDateTime createdAt;

	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	@JsonFormat(pattern = "dd/MM/yyyy hh:mm:ss a")
	private LocalDateTime updatedAt;

}
