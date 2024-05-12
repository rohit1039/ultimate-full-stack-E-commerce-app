package com.ecommerce.orderservice.verticle;

import static com.ecommerce.orderservice.constant.APIConstants.*;
import static com.ecommerce.orderservice.constant.APIConstants.ERROR_STATUS_CODE;

import com.ecommerce.orderservice.config.ConfigLoader;
import com.ecommerce.orderservice.payload.request.OrderRequest;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import com.ecommerce.orderservice.service.OrderService;
import com.ecommerce.orderservice.service.OrderServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderVerticle extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(OrderVerticle.class.getName());

  private final Router router;
  private static final Vertx vertx = Vertx.currentContext().owner();
  private static final WebClient webClient = WebClient.create(vertx);
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

    try {
      JsonObject requestBody = routingContext.body().asJsonObject();
      OrderRequest orderRequest = requestBody.mapTo(OrderRequest.class);
      long productId = Long.parseLong(routingContext.request().getParam("productId"));
      LOG.info("*** Calling product-service with productId: {} ***", productId);
      MultiMap entries = routingContext.request().params();
      updateProductCount(productId, entries);
      this.orderService
          .saveOrder(ConfigLoader.mongoConfig(), orderRequest)
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
      try {
        LOG.info("\n Incoming request: {}", new ObjectMapper().writeValueAsString(orderRequest));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    } catch (Exception e) {
      LOG.error("Some error occurred: {}", e.getMessage());
    }
  }

  private static void updateProductCount(long productId, MultiMap entries) {

    webClient
        .put(PRODUCT_PORT, HOSTNAME, PRODUCT_SERVICE + productId)
        .addQueryParam(PRODUCT_QUANTITY, entries.get(PRODUCT_QUANTITY))
        .addQueryParam(PRODUCT_SIZE, entries.get(PRODUCT_SIZE))
        .rxSend()
        .doOnError(
            error -> {
              LOG.error("Some error occurred while calling product-service: " + error.getMessage());
            })
        .subscribe(
            message ->
                LOG.info(
                    "Product Service responded with: {}",
                    message.bodyAsJsonObject() != null
                        ? message.bodyAsJsonObject().encodePrettily()
                        : message.statusCode()));
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
