package com.ecommerce.orderservice.payload.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"categoryId", "productId", "productName", "productBrand", "username",
    "productMainImage", "extraProductImages", "shortDescription", "longDescription", "createdAt",
    "updatedAt", "productPrice", "productSizes", "productCount", "productColor", "discountPercent",
    "reviewCount", "averageRating", "enabled", "inStock"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductResponse {

  private Integer productId;

  private String productName;

  private String productBrand;

  private String productMainImage;

  private String shortDescription;

  private String productColor;

  private Integer discountPercent;

  private Float discountedPrice;

  private Float totalPrice;

  private Float productPrice;

  private Integer reviewCount;

  private Float averageRating;

  private boolean enabled;

  private boolean inStock;

  public String getShortDescription() {

    return shortDescription.length() > 50 ? shortDescription.substring(0, 50).concat("...") :
        shortDescription;
  }
}
