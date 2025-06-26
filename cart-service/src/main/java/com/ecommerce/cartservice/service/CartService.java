package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.model.Cart;
import com.ecommerce.cartservice.model.CartItem;
import java.util.List;

public interface CartService {

  Cart getOrCreateCartByUsername(String username);

  Cart addToCart(String username, List<CartItem> items);

  Cart updateCartItem(String username, CartItem updatedItem);

  void removeFromCart(String username, Long productId);

  void clearCart(String username);
}
