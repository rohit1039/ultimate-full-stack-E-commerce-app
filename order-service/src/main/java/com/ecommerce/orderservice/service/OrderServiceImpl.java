package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dao.OrderDao;
import com.ecommerce.orderservice.dao.OrderDaoImpl;
import com.ecommerce.orderservice.payload.request.OrderRequest;
import io.vertx.rxjava3.ext.mongo.MongoClient;

public class OrderServiceImpl implements OrderService {

  private final OrderDao orderDao = new OrderDaoImpl();

  @Override
  public void saveOrder(MongoClient mongoClient, OrderRequest orderRequest) {

    orderDao.saveOrderIntoDB(mongoClient, orderRequest);
  }
}
