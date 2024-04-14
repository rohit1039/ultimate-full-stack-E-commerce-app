package com.ecommerce.productservice.util;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.ecommerce.productservice.controller.ProductServiceController;
import com.ecommerce.productservice.payload.response.ProductResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ProductModelAssembler
    implements RepresentationModelAssembler<ProductResponseDTO, EntityModel<ProductResponseDTO>> {

  @Override
  @NonNull
  public EntityModel<ProductResponseDTO> toModel(@NonNull ProductResponseDTO productResponseDTO) {

    return EntityModel.of(
        productResponseDTO,
        linkTo(
                methodOn(ProductServiceController.class)
                    .getProductById(productResponseDTO.getProductId()))
            .withSelfRel());
  }
}
