package com.ecommerce.orderservice.dao;

import com.ecommerce.orderservice.payload.request.OrderRequest;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import io.vertx.core.Future;
import io.vertx.rxjava3.ext.mongo.MongoClient;

public interface OrderDao {

  Future<OrderResponse> saveOrderIntoDB(MongoClient mongoClient, OrderRequest orderRequest);
}
