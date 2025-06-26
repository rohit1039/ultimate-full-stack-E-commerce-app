package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.dao.CartRepository;
import com.ecommerce.cartservice.model.Cart;
import com.ecommerce.cartservice.model.CartItem;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

  @Autowired
  private CartRepository cartRepository;

  public Cart getOrCreateCartByUsername(String username) {

    return cartRepository.findByUsername(username).orElseGet(() -> {
      Cart cart = new Cart();
      cart.setUsername(username);
      cart.setItems(new ArrayList<>());
      return cartRepository.save(cart);
    });
  }

  public Cart addToCart(String username, List<CartItem> items) {

    Cart cart = cartRepository.findByUsername(username).orElseGet(() -> {
      Cart newCart = new Cart();
      newCart.setUsername(username);
      newCart.setItems(new ArrayList<>());
      return newCart;
    });

    List<CartItem> existingItems = cart.getItems();
    if (existingItems == null) {
      existingItems = new ArrayList<>();
      cart.setItems(existingItems);
    }

    for (CartItem item : items) {
      item.setCart(cart);
      existingItems.add(item);
    }

    return cartRepository.save(cart);
  }

  public void removeFromCart(String username, Long productId) {

    Cart cartInDb = getOrCreateCartByUsername(username);
    cartInDb.getItems().removeIf(item -> {
      return item.getProductId() != null && item.getProductId().equals(productId);
    });
    cartRepository.save(cartInDb);
  }

  public Cart updateCartItem(String username, CartItem updatedItem) {

    Cart cart = getOrCreateCartByUsername(username);

    for (CartItem item : cart.getItems()) {
      if (item.getProductId().equals(updatedItem.getProductId())) {
        item.setQuantity(updatedItem.getQuantity());
        item.setProductSize(updatedItem.getProductSize());
        break;
      }
    }
    return cartRepository.save(cart);
  }

  public void clearCart(String username) {

    Cart cartInDb = getOrCreateCartByUsername(username);
    cartInDb.getItems().clear();
    cartRepository.save(cartInDb);
  }
}
