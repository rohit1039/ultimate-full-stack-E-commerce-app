package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.payload.request.OrderRequest;
import io.vertx.rxjava3.ext.mongo.MongoClient;

public interface OrderService {

  void saveOrder(MongoClient mongoClient, OrderRequest orderRequest);
}
