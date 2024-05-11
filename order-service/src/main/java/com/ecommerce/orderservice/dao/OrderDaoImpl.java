package com.ecommerce.orderservice.dao;

import com.ecommerce.orderservice.payload.request.OrderRequest;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderDaoImpl implements OrderDao {
  private static final Logger LOG = LoggerFactory.getLogger(OrderDaoImpl.class.getName());

  @Override
  public void saveOrderIntoDB(MongoClient mongoClient, OrderRequest orderRequest) {

    mongoClient.rxSave("orders", orderRequest.toJsonObject(orderRequest)).subscribe();
  }
}
