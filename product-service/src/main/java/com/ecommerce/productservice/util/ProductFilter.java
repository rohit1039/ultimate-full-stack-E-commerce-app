package com.ecommerce.productservice.util;

/**
 * This class will format our JSON response in such a way that the boolean field in both
 * Product and in ProductResponseDTO class will be excluded if value is false.
 * <p>
 * We can use @JsonInclude(value = JsonInclude.Include.NON_DEFAULT) but as the Boolean
 * have three return values TRUE, FALSE and NULL. So, even if we use this annotation it
 * won't format our JSON response i.e. it will still include even though the value is
 * false. This annotation specifically works only if the value is NULL.
 */
public class ProductFilter {

	@Override
	public boolean equals(Object value) {

		return !Boolean.valueOf(true).equals(value);
	}

}
