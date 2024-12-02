package com.ecommerce.orderservice.payload.response;

import static com.ecommerce.orderservice.constant.ApiConstants.CONTENT_TYPE;
import static com.ecommerce.orderservice.constant.ApiConstants.CREATED_STATUS_CODE;
import static com.ecommerce.orderservice.constant.ApiConstants.JSON_CONTENT_TYPE;
import static com.ecommerce.orderservice.constant.ApiConstants.SUCCESS_STATUS_CODE;

import com.ecommerce.orderservice.exception.ApiErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import java.util.List;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * This class is used to handle success and failure order responses.
 */
@Builder
@NoArgsConstructor
public class OrderResponseBuilder {

  public void handleSuccessResponse(final RoutingContext routingContext,
                                    final OrderResponse response) {

    routingContext.response()
                  .putHeader(CONTENT_TYPE, JSON_CONTENT_TYPE)
                  .setStatusCode(CREATED_STATUS_CODE)
                  .rxEnd(JsonObject.mapFrom(response).encodePrettily())
                  .subscribe();
  }

  public void handleSuccessListResponse(final RoutingContext routingContext,
                                        final List<OrderResponseList> response)
      throws JsonProcessingException {

    routingContext.response()
                  .putHeader(CONTENT_TYPE, JSON_CONTENT_TYPE)
                  .setStatusCode(SUCCESS_STATUS_CODE)
                  .rxEnd(Json.encodePrettily(OrderResponseList.toJsonList(response)))
                  .subscribe();
  }

  public void handleFailureResponse(final RoutingContext routingContext, int statusCode,
                                    final ApiErrorResponse apiErrorResponse) {

    routingContext.response()
                  .putHeader(CONTENT_TYPE, JSON_CONTENT_TYPE)
                  .setStatusCode(statusCode)
                  .rxEnd(Json.encodePrettily(apiErrorResponse))
                  .subscribe();
  }
}
