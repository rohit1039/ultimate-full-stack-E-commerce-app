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

@RestController
public class CartController {

  @Autowired
  private CartService cartService;

  @GetMapping("/get")
  public ResponseEntity<Cart> getOrCreateCart(
      @Schema(hidden = true) @RequestHeader(name = "username") String username) {

    return new ResponseEntity<>(cartService.getOrCreateCartByUsername(username), HttpStatus.OK);
  }

  @PostMapping("/add")
  public ResponseEntity<Cart> addToCart(@Schema(hidden = true) @RequestHeader String username,
                                        @RequestBody List<CartItem> items) {

    return new ResponseEntity<>(cartService.addToCart(username, items), HttpStatus.CREATED);
  }

  @PatchMapping("/update")
  public ResponseEntity<Cart> updateCart(@Schema(hidden = true) @RequestHeader String username,
                                         @RequestBody CartItem item) {

    return new ResponseEntity<>(cartService.updateCartItem(username, item), HttpStatus.OK);
  }

  @DeleteMapping("/{productId}")
  public ResponseEntity<Void> removeItem(@Schema(hidden = true) @RequestHeader String username,
                                         @PathVariable Long productId) {

    cartService.removeFromCart(username, productId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/delete")
  public ResponseEntity<Void> clearCart(@Schema(hidden = true) @RequestHeader String username) {

    cartService.clearCart(username);
    return ResponseEntity.ok().build();
  }
}
