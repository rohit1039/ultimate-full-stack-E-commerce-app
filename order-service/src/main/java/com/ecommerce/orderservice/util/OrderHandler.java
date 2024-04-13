package com.ecommerce.orderservice.util;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderHandler implements Handler<RoutingContext> {

  private static final Logger LOG = LoggerFactory.getLogger(OrderHandler.class.getName());

  @Override
  public void handle(RoutingContext routingContext) {

    JsonObject requestBody = routingContext.body().asJsonObject();
    LOG.info("\n Incoming request: {}", requestBody.encode());
    JsonArray jsonArray = new JsonArray();
    jsonArray.add(new JsonObject().put("response", "Order Placed Successfully!"));
    LOG.info("\n Path: {} responds with: {}", routingContext.normalizedPath(), jsonArray.encode());
    routingContext.response().end(jsonArray.toBuffer());
  }
}
