package com.ecommerce.orderservice.verticle;

import com.ecommerce.orderservice.util.OrderHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderVerticle extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(OrderVerticle.class.getName());

  public static void createOrder(Router parentRoute) {

    parentRoute.post("/v1/place-order").handler(new OrderHandler());
  }
}