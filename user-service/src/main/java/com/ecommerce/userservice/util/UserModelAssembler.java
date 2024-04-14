package com.ecommerce.userservice.util;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.ecommerce.userservice.controller.UserServiceController;
import com.ecommerce.userservice.payload.response.UserDTOResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class UserModelAssembler
    implements RepresentationModelAssembler<UserDTOResponse, EntityModel<UserDTOResponse>> {

  @Override
  public EntityModel<UserDTOResponse> toModel(UserDTOResponse userApiResponse) {
    /*
     * this logic is implemented to avoid some pitfalls in the URLs using UTF_8 for
     * example - instead of space, printing %20 or %40
     */
    String decodedValue =
        CustomURLDecoder.decodeValue(
            linkTo(
                    methodOn(UserServiceController.class)
                        .getUserByEmail(userApiResponse.getEmailId()))
                .withSelfRel()
                .getHref());
    return EntityModel.of(
        userApiResponse,
        linkTo(methodOn(UserServiceController.class).getUserByEmail(userApiResponse.getEmailId()))
            .withSelfRel()
            .withHref(decodedValue));
  }
}
