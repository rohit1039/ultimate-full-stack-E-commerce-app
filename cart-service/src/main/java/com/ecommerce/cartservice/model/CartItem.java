package com.ecommerce.cartservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Represents an item in a shopping cart. Each item corresponds to a specific product
 * with details such as product ID, name, quantity, size, and color. It is linked to
 * a parent {@link Cart} entity.
 *
 * This entity is managed using JPA and persists data in the "cart_item" table. It uses
 * snake_case naming strategy for JSON serialization/deserialization and supports
 * relationships with its parent cart.
 *
 * Annotations Summary:
 * - {@code @Entity}: Marks this class as a JPA entity.
 * - {@code @Table}: Specifies the database table name as "cart_item".
 * - {@code @Data}: Generates getter, setter, equals, hashcode, and toString methods.
 * - {@code @NoArgsConstructor} and {@code @AllArgsConstructor}: Create constructors for the class.
 * - {@code @ToString}: Generates the toString method, excluding sensitive fields if annotated.
 * - {@code @JsonNaming}: Specifies the naming strategy for JSON formatting.
 * - {@code @JsonIgnore}: Excludes certain fields from JSON serialization/deserialization.
 *
 * Relationships:
 * - {@link Cart}: Represents the parent cart that the item is contained in. Each cart item
 *   is associated with a single cart through a many-to-one relationship.
 */
@Entity
@Table(name = "cart_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CartItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonIgnore
  private Long id;
  @ManyToOne
  @JoinColumn(name = "cart_id")
  @JsonIgnore
  @ToString.Exclude
  private Cart cart;
  private Long productId;
  private String productName;
  private int quantity;
  private String size;
  private String color;
}
