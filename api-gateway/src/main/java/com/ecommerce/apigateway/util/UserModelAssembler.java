package com.ecommerce.apigateway.util;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.ecommerce.apigateway.controller.AuthenticationController;
import com.ecommerce.apigateway.payload.response.UserDTOResponse;
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
        CustomUrlDecoder.decodeValue(
            linkTo(
                    methodOn(AuthenticationController.class)
                        .getUserByEmail(userApiResponse.getEmailId()))
                .withSelfRel()
                .getHref());
    return EntityModel.of(
        userApiResponse,
        linkTo(
                methodOn(AuthenticationController.class)
                    .getUserByEmail(userApiResponse.getEmailId()))
            .withSelfRel()
            .withHref(decodedValue));
  }
}
