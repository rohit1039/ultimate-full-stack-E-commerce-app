package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import io.vertx.rxjava3.ext.web.RoutingContext;

public interface OrderService {

  void saveOrder(MongoClient mongoClient, OrderRequest orderRequest, RoutingContext routingContext);
}
