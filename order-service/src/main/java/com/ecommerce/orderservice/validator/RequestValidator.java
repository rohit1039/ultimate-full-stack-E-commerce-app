package com.ecommerce.orderservice.validator;

import static com.ecommerce.orderservice.constant.APIConstants.CONTENT_TYPE;
import static com.ecommerce.orderservice.constant.APIConstants.JSON_CONTENT_TYPE;

import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
public class RequestValidator {
  private static final Logger LOG = LoggerFactory.getLogger(RequestValidator.class.getName());

  JsonObject errors = new JsonObject();

  public boolean validateRequest(RoutingContext routingContext, OrderRequest orderRequest) {

    if (errors.size() > 0) {
      LOG.error("*** Request validation errors found ***");
      LOG.warn("*** Unable to process your order ***");
      routingContext
          .response()
          .putHeader(CONTENT_TYPE, JSON_CONTENT_TYPE)
          .setStatusCode(400)
          .setStatusMessage(HttpStatus.BAD_REQUEST.getReasonPhrase())
          .end(errors.encodePrettily());
    }
    return errors.size() > 0;
  }
}
