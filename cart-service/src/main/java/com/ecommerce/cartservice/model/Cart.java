package com.ecommerce.cartservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Represents a shopping cart entity in the e-commerce platform. Each cart is associated
 * with a user and contains a list of items selected by the user for purchase.
 *
 * The cart includes the following key details:
 * - A unique identifier for the cart.
 * - A username to associate the cart with a specific user account.
 * - A collection of {@link CartItem} entities representing the individual items in the cart.
 *
 * Annotations Summary:
 * - {@code @Entity}: Indicates this is a JPA entity to be persisted in the database.
 * - {@code @Data}: Automatically generates boilerplate methods such as getters, setters,
 *   equals, hashcode, and toString.
 * - {@code @AllArgsConstructor} and {@code @NoArgsConstructor}: Provide all-arguments and
 *   no-arguments constructors, respectively.
 * - {@code @ToString}: Generates a string representation of the object.
 * - {@code @Id}: Marks the primary key of the entity.
 * - {@code @GeneratedValue}: Specifies the strategy for generating primary key values.
 * - {@code @Column}: Configures the properties of the corresponding database column.
 *   This includes constraints such as `nullable = false` for mandatory fields and
 *   `unique = true` to ensure unique usernames.
 * - {@code @OneToMany}: Represents a one-to-many relationship between the cart and
 *   its items. Enables cascading operations and orphan removal to manage the life
 *   cycle of {@link CartItem} entities.
 * - {@code @Schema}: Provides metadata for API documentation, including a description
 *   and example data for the corresponding fields.
 *
 * Relationships:
 * - {@link CartItem}: Represents the items within the cart. Each cart can contain multiple
 *   cart items, and the items are managed via cascading operations and orphan removal.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Cart {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  @Schema(description = "username", example = "testuser@gmail.com")
  private String username;

  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
  @Schema(description = "Items to be added in the cart", type = "array", example = "[" +
      "{\"productId\": 101, \"size\": \"L\", \"quantity\": 1}," +
      " {\"productId\": 202, \"size\": \"M\", \"quantity\": 1}" + "]")
  private List<CartItem> items;

}
