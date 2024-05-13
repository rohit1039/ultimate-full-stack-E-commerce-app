package com.ecommerce.orderservice.verticle;

import static com.ecommerce.orderservice.constant.APIConstants.*;
import static com.ecommerce.orderservice.constant.APIConstants.ERROR_STATUS_CODE;

import com.ecommerce.orderservice.config.ConfigLoader;
import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import com.ecommerce.orderservice.service.OrderService;
import com.ecommerce.orderservice.service.OrderServiceImpl;
import com.ecommerce.orderservice.validator.RequestValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderVerticle extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(OrderVerticle.class.getName());

  private final Router router;
  private final OrderService orderService = new OrderServiceImpl();

  public OrderVerticle(Router router) {
    this.router = router;
  }

  @Override
  public void start(Promise<Void> startFuture) {
    placeOrder(router);
  }

  public void placeOrder(Router parentRoute) {
    parentRoute.post("/v1/place-order/:productId").handler(this::handle);
  }

  public void handle(RoutingContext routingContext) {

    JsonObject requestBody = routingContext.body().asJsonObject();
    OrderRequest orderRequest = requestBody.mapTo(OrderRequest.class);
    boolean validationErrors = new RequestValidator().validateRequest(routingContext, orderRequest);
    if (!validationErrors) {
      createOrder(routingContext);
    }
    try {
      LOG.info("\n Incoming request: {}", new ObjectMapper().writeValueAsString(orderRequest));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private void createOrder(RoutingContext routingContext) {

    this.orderService
        .saveOrder(ConfigLoader.mongoConfig(), routingContext)
        .onSuccess(
            response -> {
              LOG.info("*** Order placed successfully ***");
              handleSuccessResponse(routingContext, response);
            })
        .onFailure(
            error -> {
              LOG.info("Some error occurred while saving order details: {}", error.getMessage());
              handleErrorResponse(routingContext, error);
            });
  }

  private void handleSuccessResponse(RoutingContext routingContext, OrderResponse response) {

    routingContext
        .response()
        .putHeader(CONTENT_TYPE, JSON_CONTENT_TYPE)
        .setStatusCode(CREATED_STATUS_CODE)
        .rxEnd(new OrderResponse().buildOrderResponse(response.getOrderId()).encodePrettily())
        .subscribe();
  }

  private void handleErrorResponse(RoutingContext routingContext, Throwable error) {

    routingContext
        .response()
        .putHeader(CONTENT_TYPE, JSON_CONTENT_TYPE)
        .setStatusCode(ERROR_STATUS_CODE)
        .rxEnd(error.getMessage())
        .subscribe();
  }
}
