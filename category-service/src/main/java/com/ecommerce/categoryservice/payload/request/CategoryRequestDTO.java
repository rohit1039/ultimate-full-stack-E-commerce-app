package com.ecommerce.categoryservice.payload.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CategoryRequestDTO {

  @NotBlank
  @Schema(description = "Category Name", example = "Men's T-Shirts")
  private String categoryName;

  /** We can update parentCategoryName only if the parentCategory exists in our database */
  @Schema(description = "Parent Category Name", example = "Men's Wear")
  private String parentCategoryName;
}
