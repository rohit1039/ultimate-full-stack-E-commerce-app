package com.ecommerce.cartservice.controller;

import com.ecommerce.cartservice.model.Cart;
import com.ecommerce.cartservice.model.CartItem;
import com.ecommerce.cartservice.service.CartService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller handles CRUD operations for the user's shopping cart.
 * Provides endpoints to manage the user's cart including retrieving, adding, updating,
 * and deleting items, as well as clearing the cart.
 */
@RestController
public class CartController {

  @Autowired
  private CartService cartService;

  /**
   * Retrieves the shopping cart for the specified user. If the user does not have an existing cart,
   * a new cart is created and returned.
   *
   * @param username the unique identifier of the user, provided via the request header.
   * @return a ResponseEntity containing the user's {@link Cart}. The response body includes the
   *         existing or newly created cart, along with an HTTP status of OK.
   */
  @GetMapping("/get")
  public ResponseEntity<Cart> getOrCreateUserCart(
      @Schema(hidden = true) @RequestHeader(name = "username") String username) {

    return new ResponseEntity<>(cartService.getOrCreateCartByUsername(username), HttpStatus.OK);
  }

  /**
   * Adds a list of items to the user's shopping cart. If the cart does not exist, it is created.
   *
   * @param username the unique identifier of the user, provided via the request header.
   * @param items the list of {@link CartItem} objects to be added to the cart.
   * @return a ResponseEntity containing the updated {@link Cart} and an HTTP status of CREATED.
   */
  @PostMapping("/add")
  public ResponseEntity<Cart> addToUserCart(@Schema(hidden = true) @RequestHeader String username,
                                        @RequestBody List<CartItem> items) {

    return new ResponseEntity<>(cartService.addToCart(username, items), HttpStatus.CREATED);
  }

  /**
   * Updates a specific item in the user's shopping cart. If the cart or the item does not exist,
   * the operation will handle it appropriately based on the implementation in the service layer.
   *
   * @param username the unique identifier of the user, provided via the request header.
   * @param item the {@link CartItem} object containing details about the item to be updated.
   * @return a ResponseEntity containing the updated {@link Cart} and an HTTP status of OK.
   */
  @PatchMapping("/update")
  public ResponseEntity<Cart> updateUserCart(@Schema(hidden = true) @RequestHeader String username,
                                         @RequestBody CartItem item) {

    return new ResponseEntity<>(cartService.updateCartItem(username, item), HttpStatus.OK);
  }

  /**
   * Removes a specific item from the user's shopping cart.
   *
   * @param username the unique identifier of the user, provided via the request header.
   * @param productId the unique identifier of the product to be removed from the cart.
   * @return a ResponseEntity with an HTTP status of OK if the item is successfully removed.
   */
  @DeleteMapping("/{productId}")
  public ResponseEntity<Void> removeItem(@Schema(hidden = true) @RequestHeader String username,
                                         @PathVariable Long productId) {

    cartService.removeFromCart(username, productId);
    return ResponseEntity.ok().build();
  }

  /**
   * Clears all items from the user's shopping cart.
   *
   * @param username the unique identifier of the user, provided via the request header.
   * @return a ResponseEntity with an HTTP status of OK to indicate that the cart has been successfully cleared.
   */
  @DeleteMapping("/delete")
  public ResponseEntity<Void> clearCart(@Schema(hidden = true) @RequestHeader String username) {

    cartService.clearCart(username);
    return ResponseEntity.ok().build();
  }
}
