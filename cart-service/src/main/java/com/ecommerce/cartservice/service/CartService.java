package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.model.Cart;
import com.ecommerce.cartservice.model.CartItem;
import java.util.List;

/**
 * Service interface for managing shopping carts. Provides methods for retrieving,
 * creating, updating, and clearing carts, as well as operations for managing cart items.
 */
public interface CartService {

  /**
   * Retrieves an existing cart associated with the specified username or creates a new one if no such cart exists.
   * If a cart does not exist for the given username, a new cart is initialized, associated with the username,
   * and saved in the repository.
   *
   * @param username the username for which the cart is to be retrieved or created
   * @return the existing cart if found, or a newly created cart associated with the given username
   */
  Cart getOrCreateCartByUsername(String username);

  /**
   * Adds a list of items to the cart associated with the specified username. If no cart exists for the user,
   * a new cart will be created. The provided items are then added to the cart, and the updated cart is saved.
   *
   * @param username the username of the user whose cart needs to be updated
   * @param items the list of items to be added to the user's cart
   * @return the updated or newly created cart containing the added items
   */
  Cart addToCart(String username, List<CartItem> items);

  /**
   * Updates a specific item in the user's shopping cart based on the provided updated item information.
   * Searches the cart for an existing item with the same {@code productId}, then updates
   * its quantity and size values to match the provided {@code updatedItem}. The updated
   * cart is then saved and returned.
   *
   * @param username the username of the user whose cart is being updated
   * @param updatedItem the {@link CartItem} containing the updated details,
   *                    including {@code productId}, {@code quantity}, and {@code size}
   * @return the updated {@link Cart} after applying the changes to the specified item
   */
  Cart updateCartItem(String username, CartItem updatedItem);

  /**
   * Removes a product from the user's shopping cart based on the provided product ID.
   * If the specified product exists in the cart, it is removed, and the updated cart is persisted.
   *
   * @param username the username associated with the cart from which the product should be removed
   * @param productId the unique identifier of the product to remove from the cart
   */
  void removeFromCart(String username, Long productId);

  /**
   * Clears all items in the shopping cart for the specified user. If the user does not have
   * an existing cart, a new cart will be created and cleared.
   *
   * @param username the username of the user whose cart is to be cleared
   */
  void clearCart(String username);
}
