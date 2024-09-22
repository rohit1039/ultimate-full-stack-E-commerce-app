package com.ecommerce.orderservice.dao;

import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import com.ecommerce.orderservice.payload.response.OrderResponseList;
import io.vertx.core.Future;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import io.vertx.rxjava3.ext.web.RoutingContext;
import java.util.List;

public interface OrderDao {

  Future<OrderResponse> saveOrderInDb(
      MongoClient mongoClient, RoutingContext routingContext, OrderRequest orderRequest);

  Future<List<OrderResponseList>> getOrdersFromDb(MongoClient mongoClient, String username);
}
