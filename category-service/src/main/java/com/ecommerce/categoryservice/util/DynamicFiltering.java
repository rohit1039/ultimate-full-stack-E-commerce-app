package com.ecommerce.categoryservice.util;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is used to filter the fields from JSON response dynamically.
 */
public class DynamicFiltering {

	public static final Set<String> ignorableFieldNamesInCreateCategory = new HashSet<>();

	public static final Set<String> ignorableFieldNamesInGetCategories = new HashSet<>();

	public static final Set<String> ignorableFieldNamesInGetCategoryById = new HashSet<>();

	static {
		ignorableFieldNamesInCreateCategory.add("");
	}

	static {
		ignorableFieldNamesInGetCategories.add("parent");
	}

	static {
		ignorableFieldNamesInGetCategoryById.add("");
	}

}
