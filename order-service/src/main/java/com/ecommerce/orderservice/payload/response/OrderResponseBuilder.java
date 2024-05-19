package com.ecommerce.orderservice.payload.response;

import static com.ecommerce.orderservice.constant.APIConstants.CONTENT_TYPE;
import static com.ecommerce.orderservice.constant.APIConstants.CREATED_STATUS_CODE;
import static com.ecommerce.orderservice.constant.APIConstants.ERROR_STATUS_CODE;
import static com.ecommerce.orderservice.constant.APIConstants.JSON_CONTENT_TYPE;

import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.Builder;
import lombok.NoArgsConstructor;

/** This class is used to handle success and failure order responses. */
@Builder
@NoArgsConstructor
public class OrderResponseBuilder {

  public void handleSuccessResponse(
      final RoutingContext routingContext, final OrderResponse response) {

    routingContext
        .response()
        .putHeader(CONTENT_TYPE, JSON_CONTENT_TYPE)
        .setStatusCode(CREATED_STATUS_CODE)
        .rxEnd(new OrderResponse().buildOrderResponse(response.getOrderId()).encodePrettily())
        .subscribe();
  }

  public void handleFailureResponse(final RoutingContext routingContext, final Throwable error) {

    routingContext
        .response()
        .putHeader(CONTENT_TYPE, JSON_CONTENT_TYPE)
        .setStatusCode(ERROR_STATUS_CODE)
        .rxEnd(error.getMessage())
        .subscribe();
  }
}
