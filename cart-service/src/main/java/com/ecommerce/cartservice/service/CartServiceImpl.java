package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.dao.CartRepository;
import com.ecommerce.cartservice.model.Cart;
import com.ecommerce.cartservice.model.CartItem;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the {@link CartService} interface that provides operations for managing user shopping carts.
 * This service interacts with the {@link CartRepository} to perform CRUD operations on carts and their associated items.
 *
 * The class is annotated with {@code @Service} to indicate that it's a service component and {@code @Transactional}
 * to allow transaction management.
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {

  @Autowired
  private CartRepository cartRepository;

  /**
   * Retrieves an existing cart associated with the specified username or creates a new one if no such cart exists.
   * If a cart does not exist for the given username, a new cart is initialized, associated with the username,
   * and saved in the repository.
   *
   * @param username the username for which the cart is to be retrieved or created
   * @return the existing cart if found, or a newly created cart associated with the given username
   */
  public Cart getOrCreateCartByUsername(String username) {

    return cartRepository.findByUsername(username).orElseGet(() -> {
      Cart cart = new Cart();
      cart.setUsername(username);
      cart.setItems(new ArrayList<>());
      return cartRepository.save(cart);
    });
  }

  /**
   * Adds a list of items to the cart associated with the specified username. If a cart does not
   * exist for the given username, a new cart is created. The provided items are added to the cart,
   * and the cart is then saved to the repository.
   *
   * @param username the username of the user whose cart needs to be updated
   * @param items the list of items to be added to the user's cart
   * @return the updated or newly created cart containing the added items
   */
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

  /**
   * Removes a product from the user's shopping cart based on the provided productId.
   * If the product exists in the cart, it is removed. The changes are then saved to the database.
   *
   * @param username the username associated with the cart from which the product should be removed
   * @param productId the ID of the product to be removed from the cart
   */
  public void removeFromCart(String username, Long productId) {

    Cart cartInDb = getOrCreateCartByUsername(username);
    cartInDb.getItems().removeIf(item -> {
      return item.getProductId() != null && item.getProductId().equals(productId);
    });
    cartRepository.save(cartInDb);
  }

  /**
   * Updates a specific item in the user's cart based on the provided updated item information.
   * Searches the cart for an existing item with the same {@code productId}, then updates
   * its quantity and size values to match the provided {@code updatedItem}. The updated
   * cart is then saved and returned.
   *
   * @param username the username of the user whose cart is being updated
   * @param updatedItem the {@link CartItem} containing the updated details,
   *                    including {@code productId}, {@code quantity}, and {@code size}
   * @return the updated {@link Cart} after applying the changes to the specified item
   */
  public Cart updateCartItem(String username, CartItem updatedItem) {

    Cart cart = getOrCreateCartByUsername(username);

    for (CartItem item : cart.getItems()) {
      if (item.getProductId().equals(updatedItem.getProductId())) {
        item.setQuantity(updatedItem.getQuantity());
        item.setSize(updatedItem.getSize());
        break;
      }
    }
    return cartRepository.save(cart);
  }

  /**
   * Clears all items in the shopping cart for the specified user. If the user does not have an
   * existing cart, a new cart will be created and cleared.
   *
   * @param username the username of the user whose cart is to be cleared
   */
  public void clearCart(String username) {

    Cart cartInDb = getOrCreateCartByUsername(username);
    cartInDb.getItems().clear();
    cartRepository.save(cartInDb);
  }
}
