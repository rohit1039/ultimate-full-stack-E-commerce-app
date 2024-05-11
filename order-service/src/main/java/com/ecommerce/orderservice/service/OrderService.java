package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.payload.request.OrderRequest;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import io.vertx.core.Future;
import io.vertx.rxjava3.ext.mongo.MongoClient;

public interface OrderService {

  Future<OrderResponse> saveOrder(MongoClient mongoClient, OrderRequest orderRequest);
}
