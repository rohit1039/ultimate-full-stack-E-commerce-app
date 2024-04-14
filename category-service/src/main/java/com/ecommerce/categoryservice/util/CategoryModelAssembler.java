package com.ecommerce.categoryservice.util;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.ecommerce.categoryservice.controller.CategoryServiceController;
import com.ecommerce.categoryservice.payload.response.CategoryResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class CategoryModelAssembler
    implements RepresentationModelAssembler<CategoryResponseDTO, EntityModel<CategoryResponseDTO>> {

  @Override
  @NonNull
  public EntityModel<CategoryResponseDTO> toModel(
      @NonNull CategoryResponseDTO categoryResponseDTO) {

    return EntityModel.of(
        categoryResponseDTO,
        linkTo(
                methodOn(CategoryServiceController.class)
                    .getCategoryById(categoryResponseDTO.getCategoryId()))
            .withSelfRel());
  }
}
