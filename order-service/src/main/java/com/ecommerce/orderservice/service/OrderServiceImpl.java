package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dao.OrderDao;
import com.ecommerce.orderservice.dao.OrderDaoImpl;
import com.ecommerce.orderservice.payload.request.OrderRequest;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import io.vertx.core.Future;
import io.vertx.rxjava3.ext.mongo.MongoClient;

public class OrderServiceImpl implements OrderService {

  private final OrderDao orderDao = new OrderDaoImpl();

  @Override
  public Future<OrderResponse> saveOrder(MongoClient mongoClient, OrderRequest orderRequest) {

    return orderDao.saveOrderIntoDB(mongoClient, orderRequest);
  }
}
