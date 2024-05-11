package com.ecommerce.orderservice.util;

import static com.ecommerce.orderservice.constant.APIConstants.*;

import com.ecommerce.orderservice.config.ConfigLoader;
import com.ecommerce.orderservice.payload.request.OrderRequest;
import com.ecommerce.orderservice.service.OrderService;
import com.ecommerce.orderservice.service.OrderServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderHandler implements Handler<RoutingContext> {

  private static final Logger LOG = LoggerFactory.getLogger(OrderHandler.class.getName());
  private static final Vertx vertx = Vertx.currentContext().owner();
  private static final WebClient webClient = WebClient.create(vertx);
  private final OrderService orderService = new OrderServiceImpl();

  @Override
  public void handle(RoutingContext routingContext) {

    ObjectMapper objectMapper = new ObjectMapper();
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
              routingContext
                  .response()
                  .putHeader(CONTENT_TYPE, JSON_CONTENT_TYPE)
                  .setStatusCode(CREATED_STATUS_CODE)
                  .end(new JsonObject().put("order_id", response.getOrderId()).encodePrettily());
            })
        .onFailure(
            error -> {
              LOG.info("Some error occurred while saving order details: {}", error.getMessage());
              routingContext
                  .response()
                  .putHeader(CONTENT_TYPE, JSON_CONTENT_TYPE)
                  .setStatusCode(ERROR_STATUS_CODE)
                  .end(error.getMessage());
            });
    try {
      LOG.info("\n Incoming request: {}", objectMapper.writeValueAsString(orderRequest));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
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
                    "\n Product Service responded with: {} \n",
                    message.bodyAsJsonObject() != null
                        ? message.bodyAsJsonObject().encodePrettily()
                        : message.statusCode()));
  }
}
