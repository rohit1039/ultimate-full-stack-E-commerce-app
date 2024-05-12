package com.ecommerce.orderservice.validator;

import static com.ecommerce.orderservice.constant.APIConstants.CONTENT_TYPE;
import static com.ecommerce.orderservice.constant.APIConstants.JSON_CONTENT_TYPE;
import static java.util.Objects.isNull;

import com.ecommerce.orderservice.payload.request.OrderRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
public class RequestValidator {
  private static final Logger LOG = LoggerFactory.getLogger(RequestValidator.class.getName());

  JsonObject errors = new JsonObject();

  public boolean validateRequest(RoutingContext routingContext, OrderRequest orderRequest) {

    if (StringUtils.isBlank(orderRequest.getFullName()) || isNull(orderRequest.getFullName())) {
      errors.put("full_name", "full_name cannot be null or empty");
    }
    if (StringUtils.isBlank(orderRequest.getAddressLine1Txt())
        || isNull(orderRequest.getAddressLine1Txt())) {
      errors.put("address_line1_txt", "address_line1_txt is mandatory to place an order");
    }
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
