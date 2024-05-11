package com.ecommerce.orderservice.dao;

import com.ecommerce.orderservice.payload.request.OrderRequest;
import io.vertx.rxjava3.ext.mongo.MongoClient;

public interface OrderDao {

  void saveOrderIntoDB(MongoClient mongoClient, OrderRequest orderRequest);
}
