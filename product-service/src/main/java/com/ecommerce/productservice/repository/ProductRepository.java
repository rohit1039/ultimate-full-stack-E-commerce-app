package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * The ProductRepository interface defines the methods that the ProductService uses to
 * interact with the database. The interface extends the MongoRepository interface, which
 * provides default methods for working with MongoDB. The methods in the interface are
 * used to retrieve, save, update, and delete Product objects from the database. The
 * findByProductName method is used to retrieve a list of products based on the product
 * name.
 *
 */
public interface ProductRepository extends MongoRepository<Product, Long> {

	/**
	 * Returns a list of products that match the specified product name.
	 * @param productName the product name to search for
	 * @return a list of products that match the specified product name
	 */
	List<Product> findByProductName(String productName);

}
