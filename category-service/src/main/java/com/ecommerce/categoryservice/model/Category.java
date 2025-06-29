package com.ecommerce.categoryservice.model;

import com.ecommerce.categoryservice.util.CategoryFilter;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@ToString
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long categoryId;

  @Column(name = "category_name", nullable = false, unique = true)
  private String categoryName;

  @Column(name = "enabled")
  private boolean enabled;

  @Column(name = "created_by", nullable = false)
  private String createdBy;

  @Column(name = "updated_by")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String updatedBy;

  @OneToOne
  @JoinColumn(name = "parent_id")
  @JsonBackReference
  private Category parent;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
  @OrderBy("categoryName asc")
  @JsonManagedReference
  @JsonIgnore
  private Set<Category> children = new HashSet<>();

  @Transient
  @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = CategoryFilter.class)
  private boolean hasChildren;

  @Column(name = "created_at")
  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  @JsonFormat(pattern = "dd/MM/yyyy hh:mm:ss a")
  private LocalDateTime createdAt;

  @Column(name = "last_updated_at")
  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  @JsonFormat(pattern = "dd/MM/yyyy hh:mm:ss a")
  private LocalDateTime updatedAt;

  public static Category copyFull(Category category) {

    Category copyCategory = new Category();
    copyCategory.setCategoryId(category.getCategoryId());
    copyCategory.setCategoryName(category.getCategoryName());
    copyCategory.setParent(category.getParent());
    copyCategory.setEnabled(category.isEnabled());
    copyCategory.setCreatedBy(category.getCreatedBy());
    copyCategory.setUpdatedBy(category.getUpdatedBy());
    copyCategory.setCreatedAt(category.getCreatedAt());
    copyCategory.setUpdatedAt(category.getUpdatedAt());
    copyCategory.setHasChildren(category.getChildren().size() > 0);
    return copyCategory;
  }

  public static Category copyFull(Category category, String name) {

    Category copyCategory = Category.copyFull(category);
    copyCategory.setCategoryName(name);
    return copyCategory;
  }
}
