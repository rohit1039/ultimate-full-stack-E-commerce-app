package com.ecommerce.categoryservice.service;

import static java.util.Objects.isNull;

import com.ecommerce.categoryservice.exception.CategoryDuplicationException;
import com.ecommerce.categoryservice.exception.CategoryNotFoundException;
import com.ecommerce.categoryservice.exception.UnAuthorizedException;
import com.ecommerce.categoryservice.model.Category;
import com.ecommerce.categoryservice.payload.request.CategoryRequestDTO;
import com.ecommerce.categoryservice.payload.response.CategoryResponseDTO;
import com.ecommerce.categoryservice.repository.CategoryRepository;
import com.ecommerce.categoryservice.repository.FilterableCategoryRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** This class provides the implementation of the CategoryService. */
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CategoryServiceImpl.class);

  private final CategoryRepository categoryRepository;

  private final ModelMapper modelMapper;

  private final FilterableCategoryRepository filterableCategoryRepository;

  /**
   * This method saves a new Category.
   *
   * @param categoryDTO the CategoryDTO object
   * @param username the username of the user
   * @return the CategoryResponseDTO object
   */
  @Override
  public CategoryResponseDTO saveCategory(
      CategoryRequestDTO categoryDTO, String username, String role) {
    // Check if the user has the required role
    if (!isNull(role) && role.equals("ROLE_USER")) {
      LOGGER.error("*** {} ***", "Role needs to be ADMIN to save category");
      throw new UnAuthorizedException("Requires ROLE_ADMIN to create a category");
    }
    // Check if the category name is unique
    if (categoriesIsUnique(0L, categoryDTO.getCategoryName())) {
      // Save the category
      Category savedCategory;
      Category category = new Category();
      category.setCategoryName(categoryDTO.getCategoryName());
      category.setEnabled(true);
      category.setCreatedBy(username);
      category.setCreatedAt(LocalDateTime.now());
      // Check if the parent category exists
      if (isNull(categoryDTO.getParentCategoryName())
          || categoryDTO.getParentCategoryName().isEmpty()) {
        savedCategory = this.categoryRepository.save(category);
        LOGGER.info("***** Parent category saved successfully *****");
      } else {
        Category parentCategory =
            this.categoryRepository.findByParentCategory(
                categoryDTO.getParentCategoryName().replaceAll("--", ""));
        if (isNull(parentCategory)) {
          LOGGER.error("***** Parent category doesn't exists *****");
          throw new CategoryNotFoundException("Parent category doesn't exists");
        }
        category.setParent(parentCategory);
        savedCategory = this.categoryRepository.save(category);
        LOGGER.info("***** Sub category saved successfully *****");
      }
      // Map the saved category to a CategoryResponseDTO
      CategoryResponseDTO categoryResponseDTO =
          this.modelMapper.map(savedCategory, CategoryResponseDTO.class);
      // Set the hasChildren flag for the parent category
      if (categoryResponseDTO.getParent() != null) {
        categoryResponseDTO
            .getParent()
            .setHasChildren(!categoryResponseDTO.getParent().getChildren().isEmpty());
      }
      return categoryResponseDTO;
    } else {
      LOGGER.error("***** Categories cannot be duplicated *****");
      throw new CategoryDuplicationException("Categories already exists");
    }
  }

  /**
   * This method returns a list of all categories.
   *
   * @param filterFields the search key
   * @return the list of CategoryResponseDTO objects
   */
  @Override
  public List<CategoryResponseDTO> findAllCategories(
      Map<String, Object> filterFields, String role) {
    // Get the categories
    List<Category> categories;
    if (!filterFields.isEmpty()) {
      categories = this.filterableCategoryRepository.listWithFilter(filterFields);
    } else {
      categories = this.categoryRepository.findAll();
    }
    // Check if any categories were found
    if (categories.isEmpty()) {
      throw new CategoryNotFoundException("***** No categories found *****");
    }
    // Check if a search key was provided
    if (!filterFields.isEmpty()) {
      // Get the search results
      List<Category> searchResult = this.filterableCategoryRepository.listWithFilter(filterFields);
      // Check if any results were found
      if (!searchResult.isEmpty()) {
        // Set the hasChildren flag for each result
        for (Category category : searchResult) {
          category.setHasChildren(!category.getChildren().isEmpty());
        }
        // Map the results to CategoryResponseDTOs
        return searchResult.stream()
            .map(category -> this.modelMapper.map(category, CategoryResponseDTO.class))
            .collect(Collectors.toList());
      } else {
        throw new CategoryNotFoundException("***** No categories found to display *****");
      }
    } else {
      // List the categories hierarchically
      return listHierarchicalCategories(categories, role);
    }
  }

  /**
   * This method returns a Category by ID.
   *
   * @param categoryId the ID of the Category
   * @return the CategoryResponseDTO object
   */
  @Override
  public CategoryResponseDTO findCategoryById(Long categoryId) {
    // Get the category
    Optional<Category> category = this.categoryRepository.findById(categoryId);
    if (category.isPresent() && category.get().isEnabled()) {
      // Map the category to a CategoryResponseDTO
      return this.modelMapper.map(category, CategoryResponseDTO.class);
    }
    throw new CategoryNotFoundException("Category not found with ID: " + categoryId);
  }

  /**
   * This method updates an existing Category.
   *
   * @param categoryId the ID of the Category
   * @param categoryDTO the CategoryDTO object
   * @param username the username of the user
   * @return the CategoryResponseDTO object
   */
  @Override
  public CategoryResponseDTO updateCategory(
      Long categoryId, CategoryRequestDTO categoryDTO, String username, String role) {
    // Check if the user has the required role
    if (!isNull(role) && role.equals("ROLE_USER")) {
      LOGGER.error("*** {} ***", "Role needs to be ADMIN to update category");
      throw new UnAuthorizedException("Requires ROLE_ADMIN to update a category");
    }
    // Get the category
    Category categoryInDB =
        this.categoryRepository
            .findById(categoryId)
            .orElseThrow(
                () ->
                    new CategoryNotFoundException(
                        "Category not" + " found with ID: " + categoryId));
    // Check if the category name is unique
    if (categoriesIsUnique(categoryId, categoryDTO.getCategoryName())) {
      // Update the category
      categoryInDB.setCategoryName(categoryDTO.getCategoryName().replaceAll("--", ""));
      categoryInDB.setUpdatedAt(LocalDateTime.now());
      categoryInDB.setUpdatedBy(username);
      // Check if the parent category exists
      Category parentCategory = null;
      if (!isNull(categoryDTO.getParentCategoryName())
          && !categoryDTO.getParentCategoryName().isEmpty()) {
        parentCategory =
            this.categoryRepository.findByParentCategory(
                categoryDTO.getParentCategoryName().replaceAll("--", ""));
        if (isNull(parentCategory)) {
          LOGGER.error("***** Parent Category doesn't exists *****");
          throw new CategoryNotFoundException("Parent Category doesn't exists");
        }
      }
      categoryInDB.setParent(parentCategory);
      // Save the updated category
      Category updatedCategory = this.categoryRepository.save(categoryInDB);
      LOGGER.info("***** Category with ID: {} got updated successfully! *****", categoryId);
      return this.modelMapper.map(updatedCategory, CategoryResponseDTO.class);
    } else {
      LOGGER.error("***** Categories cannot be duplicated *****");
      throw new CategoryDuplicationException("Category already exists");
    }
  }

  /**
   * @param categoryId
   * @param status
   * @param username
   */
  @Override
  public void updateStatusCategory(Long categoryId, boolean status, String username, String role) {
    // Check if the user has the required role
    if (!isNull(role) && role.equals("ROLE_USER")) {
      LOGGER.error("*** {} ***", "Role needs to be ADMIN to update status of category");
      throw new UnAuthorizedException("Requires ROLE_ADMIN to update status of a category");
    }
    Category categoryInDB =
        this.categoryRepository
            .findById(categoryId)
            .orElseThrow(
                () ->
                    new CategoryNotFoundException(
                        "Category " + "not" + " found with ID: " + categoryId));
    categoryInDB.setUpdatedAt(LocalDateTime.now());
    categoryInDB.setUpdatedBy(username);
    categoryInDB.setEnabled(status);
    this.categoryRepository.save(categoryInDB);
    LOGGER.info(
        "***** Category with ID: " + categoryId + " status updated: {} successfully *****", status);
  }

  /**
   * This method lists the hierarchical categories.
   *
   * @param rootCategories the root categories
   * @return the list of hierarchical categories
   */
  public List<CategoryResponseDTO> listHierarchicalCategories(
      List<Category> rootCategories, String role) {
    // create an empty list to hold the hierarchical categories
    Set<Category> hierarchicalCategories = new HashSet<>();
    // loop through each root category
    for (Category rootCategory : rootCategories) {
      // check if the root category has no parent
      if (isNull(rootCategory.getParent())) {
        // add the root category to the list of hierarchical categories
        hierarchicalCategories.add(Category.copyFull(rootCategory));
        // get the child categories of the root category
        List<Category> children = rootCategory.getChildren().stream().toList();
        // loop through each child category
        for (Category subCategory : children) {
          // create a name for the sub-category with dashes
          String name = "--" + subCategory.getCategoryName();
          // add the sub-category to the list of hierarchical categories with
          // the name
          hierarchicalCategories.add(Category.copyFull(subCategory, name));
          // recursively call this method for the sub-category's children
          listSubHierarchicalCategories(hierarchicalCategories, subCategory, 1);
        }
      }
    }
    // map the hierarchical categories to CategoryResponseDTOs and return the list
    if (!isNull(role) && role.equals("ROLE_USER")) {
      return hierarchicalCategories.stream()
          .filter(Category::isEnabled)
          .map(category -> this.modelMapper.map(category, CategoryResponseDTO.class))
          .toList();
    } else {
      return hierarchicalCategories.stream()
          .map(category -> this.modelMapper.map(category, CategoryResponseDTO.class))
          .toList();
    }
  }

  /**
   * This method is used to list the sub-hierarchical categories.
   *
   * @param hierarchicalCategories the list of hierarchical categories
   * @param parent the parent category
   * @param subLevel the current subLevel
   */
  public void listSubHierarchicalCategories(
      Set<Category> hierarchicalCategories, Category parent, int subLevel) {
    // Get the children categories of the parent category
    Set<Category> children = parent.getChildren();
    // Increase the subLevel by 1
    int newSubLevel = subLevel + 1;
    // Loop through each child category
    for (Category subCategory : children) {
      // Create a string with dashes for the subLevel
      String name = "--".repeat(Math.max(0, newSubLevel)) + subCategory.getCategoryName();
      // Add the child category to the list with the subLevel string
      hierarchicalCategories.add(Category.copyFull(subCategory, name));
      // Recursively call this method for the child category's children
      listSubHierarchicalCategories(hierarchicalCategories, subCategory, newSubLevel);
    }
  }

  /**
   * This method is used to check if category is unique
   *
   * @param categoryId id of the category
   * @param categoryName name of the category
   * @return true if category is unique
   */
  public Boolean categoriesIsUnique(Long categoryId, String categoryName) {
    // Get the category from the database
    Category categoryInDB =
        this.categoryRepository.findByCategoryName(categoryName.replaceAll("--", ""));
    if (categoryId == 0) {
      return categoryInDB == null || !categoryInDB.getCategoryName().equals(categoryName);
    }
    return categoryInDB == null || Objects.equals(categoryInDB.getCategoryId(), categoryId);
  }
}
