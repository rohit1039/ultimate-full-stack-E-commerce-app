package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import io.vertx.rxjava3.ext.web.RoutingContext;

public interface OrderService {

  void retrieveOrders(MongoClient mongoClient, String username, RoutingContext routingContext);

  void saveOrder(MongoClient mongoClient, String username, OrderRequest orderRequest, RoutingContext routingContext);
}
