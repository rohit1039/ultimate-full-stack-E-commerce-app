package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.exception.ApiErrorResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import io.vertx.rxjava3.ext.web.RoutingContext;
import java.util.List;

public interface OrderService {

  void saveOrder(MongoClient mongoClient, JsonObject requestBody, String username, String contactNumber,
                 List<ApiErrorResponse> errorResponses, RoutingContext routingContext);

  void updateOrderById(MongoClient mongoClient, String orderId, String orderStatus,
                       RoutingContext routingContext);

  void retrieveOrders(MongoClient mongoClient, String username, RoutingContext routingContext);

  void updateOrderStats(MongoClient mongoClient, String orderId, String paymentStatus, String paymentMethod, RoutingContext routingContext);

  void retrieveAllOrders(MongoClient mongoClient, RoutingContext routingContext);
}
