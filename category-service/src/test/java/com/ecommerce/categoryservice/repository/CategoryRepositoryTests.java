package com.ecommerce.categoryservice.repository;

import com.ecommerce.categoryservice.model.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.Set;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
public class CategoryRepositoryTests {

	@Autowired
	private CategoryRepository repo;

	@Test
	public void testGetCategory() {

		Category category = repo.findById(1L).get();
		System.out.println(category.getCategoryName());
		Set<Category> children = category.getChildren();
		for (Category subCategory : children) {
			System.out.println(subCategory.getCategoryName());
		}

	}

	@Test
	public void testPrintHierarchicalCategories() {

		Iterable<Category> categories = repo.findAll();
		for (Category category : categories) {
			if (category.getParent() == null) {
				System.out.println(category.getCategoryName());
				Set<Category> children = category.getChildren();
				for (Category subCategory : children) {
					System.out.println("--" + subCategory.getCategoryName());
					printChildren(subCategory, 1);
				}
			}
		}
	}

	private void printChildren(Category parent, int subLevel) {

		int newSubLevel = subLevel + 1;
		Set<Category> children = parent.getChildren();
		for (Category subCategory : children) {
			for (int i = 0; i < newSubLevel; i++) {
				System.out.print("--");
			}
			System.out.println(subCategory.getCategoryName());
			printChildren(subCategory, newSubLevel);
		}
	}

}
