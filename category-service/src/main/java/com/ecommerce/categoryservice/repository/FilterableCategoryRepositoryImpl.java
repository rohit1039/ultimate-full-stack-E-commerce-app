package com.ecommerce.categoryservice.repository;

import com.ecommerce.categoryservice.model.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Repository
public class FilterableCategoryRepositoryImpl implements FilterableCategoryRepository {

	@Autowired
	private EntityManager entityManager;

	@Override
	public List<Category> listWithFilter(Map<String, Object> filterFields) {

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Category> entityQuery = builder.createQuery(Category.class);

		Root<Category> entityRoot = entityQuery.from(Category.class);
		Predicate[] predicates = new Predicate[filterFields.size()];

		if (!filterFields.isEmpty()) {

			Iterator<String> iterator = filterFields.keySet().iterator();

			int i = 0;
			while (iterator.hasNext()) {

				String fieldName = iterator.next();
				Object filterValue = filterFields.get(fieldName);
				String likeSearchText = filterValue + "%";
				predicates[i++] = builder.like(entityRoot.get(fieldName), likeSearchText);
				entityQuery.where(predicates);
			}
		}
		TypedQuery<Category> typedQuery = entityManager.createQuery(entityQuery);

		return typedQuery.getResultList();
	}

}
