package com.ecommerce.categoryservice.repository;

import com.ecommerce.categoryservice.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	@Query("SELECT c from Category c where c.categoryName = ?1")
	Category findByParentCategory(String categoryName);

	@Query("SELECT c from Category c WHERE c.categoryName = ?1")
	Category findByCategoryName(String categoryName);

}
