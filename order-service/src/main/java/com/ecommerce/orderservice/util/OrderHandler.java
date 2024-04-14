package com.ecommerce.orderservice.util;

import static com.ecommerce.orderservice.constant.APIConstants.*;

import com.ecommerce.orderservice.payload.request.OrderRequest;
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

  private static final WebClient vertxClient = WebClient.create(Vertx.currentContext().owner());

  @Override
  public void handle(RoutingContext routingContext) {

    JsonObject requestBody = routingContext.body().asJsonObject();
    ObjectMapper objectMapper = new ObjectMapper();
    OrderRequest orderRequest = requestBody.mapTo(OrderRequest.class);
    JsonObject response = new JsonObject();
    long productId = Long.parseLong(routingContext.request().getParam("productId"));
    MultiMap entries = routingContext.request().params();
    LOG.info("*** Calling product-service with productId: {} ***", productId);
    updateProductCount(routingContext, response, productId, entries);
    try {
      LOG.info("\n Incoming request: {}", objectMapper.writeValueAsString(orderRequest));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private static void updateProductCount(
      RoutingContext routingContext, JsonObject response, long productId, MultiMap entries) {

    vertxClient
        .put(PRODUCT_PORT, HOSTNAME, PRODUCT_SERVICE + productId)
        .addQueryParam(PRODUCT_QUANTITY, entries.get(PRODUCT_QUANTITY))
        .addQueryParam(PRODUCT_SIZE, entries.get(PRODUCT_SIZE))
        .rxSend()
        .doOnSuccess(
            apiResponse -> {
              if (apiResponse.statusCode() == SUCCESS_STATUS_CODE) {
                response.put("response", "Order placed successfully!");
                routingContext
                    .response()
                    .setStatusCode(CREATED_STATUS_CODE)
                    .putHeader(CONTENT_TYPE, JSON_CONTENT_TYPE)
                    .end(response.encodePrettily());
              } else {
                routingContext
                    .response()
                    .setStatusCode(apiResponse.statusCode())
                    .putHeader(CONTENT_TYPE, JSON_CONTENT_TYPE)
                    .end();
                LOG.error("Some error occurred in Product Service: {}", apiResponse.statusCode());
              }
              LOG.info(
                  "\n Path: {} responds with: {}",
                  routingContext.normalizedPath(),
                  response.encode());
            })
        .doOnError(
            error ->
                LOG.error(
                    "Some error occurred while calling product-service: " + error.getMessage()))
        .subscribe(
            message ->
                LOG.info(
                    "\n Product Service responded with: {} \n",
                    message.bodyAsJsonObject() != null
                        ? message.bodyAsJsonObject().encodePrettily()
                        : message.statusCode()));
  }
}
