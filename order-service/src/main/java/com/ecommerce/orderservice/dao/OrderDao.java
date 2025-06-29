package com.ecommerce.orderservice.dao;

import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import com.ecommerce.orderservice.payload.response.OrderResponseList;
import io.vertx.core.Future;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import java.util.List;

public interface OrderDao {

  Future<OrderResponse> saveOrder(MongoClient mongoClient, OrderRequest orderRequest,
                                  String username, String contactNumber, String token);

  Future<OrderResponse> updateOrder(MongoClient mongoClient, String orderId, String orderStatus);

  Future<OrderResponse> updateOrderStats(MongoClient mongoClient, String orderId,
                                         String paymentStatus, String paymentMethod);

  Future<List<OrderResponseList>> getAllOrders(MongoClient mongoClient);

  Future<List<OrderResponseList>> getOrdersByUsername(MongoClient mongoClient, String username);
}
