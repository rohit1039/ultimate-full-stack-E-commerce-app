package com.ecommerce.cartservice.dao;

import com.ecommerce.cartservice.model.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

  Optional<Cart> findByUsername(String username);
}
