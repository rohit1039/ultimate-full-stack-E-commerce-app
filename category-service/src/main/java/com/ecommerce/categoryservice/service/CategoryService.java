package com.ecommerce.categoryservice.service;

import com.ecommerce.categoryservice.payload.request.CategoryRequestDTO;
import com.ecommerce.categoryservice.payload.response.CategoryResponseDTO;

import java.util.List;
import java.util.Map;

public interface CategoryService {

	CategoryResponseDTO saveCategory(CategoryRequestDTO categoryDTO, String username, String role) throws Exception;

	List<CategoryResponseDTO> findAllCategories(Map<String, Object> filterFields, String role);

	CategoryResponseDTO findCategoryById(Long categoryId);

	CategoryResponseDTO updateCategory(Long categoryId, CategoryRequestDTO categoryRequestDTO, String username,
			String role) throws Exception;

	void updateStatusCategory(Long categoryId, boolean status, String username, String role) throws Exception;

}
