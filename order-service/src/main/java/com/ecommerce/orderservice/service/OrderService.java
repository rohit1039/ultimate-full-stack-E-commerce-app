package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.payload.response.OrderResponse;
import io.vertx.core.Future;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import io.vertx.rxjava3.ext.web.RoutingContext;

public interface OrderService {

  Future<OrderResponse> saveOrder(MongoClient mongoClient, RoutingContext routingContext);
}
