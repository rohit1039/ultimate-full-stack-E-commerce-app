package com.ecommerce.categoryservice.util;

/**
 * This class will format our JSON response in such a way that the hasChildren field in both
 * Category and in CategoryResponseDTO class will be excluded if value is false.
 *
 * <p>We can use @JsonInclude(value = JsonInclude.Include.NON_DEFAULT) but as the Boolean have three
 * return values TRUE, FALSE and NULL. So, even if we use this annotation it won't format our JSON
 * response i.e. it will still include even though the value is false. This annotation specifically
 * works only if the value is NULL.
 */
public class CategoryFilter {

  @Override
  public boolean equals(Object value) {

    return !Boolean.valueOf(true).equals(value);
  }
}
