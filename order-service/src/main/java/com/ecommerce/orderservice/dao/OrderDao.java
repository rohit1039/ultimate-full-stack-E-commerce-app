package com.ecommerce.orderservice.dao;

import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import io.vertx.core.Future;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import io.vertx.rxjava3.ext.web.RoutingContext;

public interface OrderDao {

  Future<OrderResponse> saveOrderInDb(
      MongoClient mongoClient, RoutingContext routingContext, OrderRequest orderRequest);
}
