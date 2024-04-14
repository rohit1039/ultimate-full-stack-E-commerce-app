package com.ecommerce.categoryservice.repository;

import com.ecommerce.categoryservice.model.Category;
import java.util.List;
import java.util.Map;

public interface FilterableCategoryRepository {

  List<Category> listWithFilter(Map<String, Object> filterFields);
}
