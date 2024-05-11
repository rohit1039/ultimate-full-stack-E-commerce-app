package com.ecommerce.orderservice.dao;

import com.ecommerce.orderservice.payload.request.OrderRequest;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import io.vertx.core.Future;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderDaoImpl implements OrderDao {
  private static final Logger LOG = LoggerFactory.getLogger(OrderDaoImpl.class.getName());

  @Override
  public Future<OrderResponse> saveOrderIntoDB(MongoClient mongoClient, OrderRequest orderRequest) {

    OrderResponse orderResponse = new OrderResponse();
    return Future.future(
        f ->
            mongoClient
                .rxSave("orders", orderRequest.toJsonObject(orderRequest))
                .doFinally(mongoClient::rxClose)
                .subscribe(
                    orderId -> {
                      orderResponse.setOrderId(orderId);
                      f.complete(orderResponse);
                    },
                    exception -> {
                      LOG.error("Database error occurred: {}", exception.getMessage());
                    }));
  }
}
