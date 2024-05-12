package com.ecommerce.orderservice.service;

import static com.ecommerce.orderservice.constant.APIConstants.*;
import static com.ecommerce.orderservice.constant.APIConstants.PRODUCT_SIZE;

import com.ecommerce.orderservice.dao.OrderDao;
import com.ecommerce.orderservice.dao.OrderDaoImpl;
import com.ecommerce.orderservice.payload.request.OrderRequest;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderServiceImpl implements OrderService {

  private static final Logger LOG = LoggerFactory.getLogger(OrderServiceImpl.class.getName());

  private final OrderDao orderDao = new OrderDaoImpl();
  private static final Vertx vertx = Vertx.currentContext().owner();
  private static final WebClient webClient = WebClient.create(vertx);

  @Override
  public Future<OrderResponse> saveOrder(MongoClient mongoClient, RoutingContext routingContext) {

    JsonObject requestBody = routingContext.body().asJsonObject();
    OrderRequest orderRequest = requestBody.mapTo(OrderRequest.class);
    MultiMap entries = routingContext.request().params();
    long productId = Long.parseLong(routingContext.request().getParam("productId"));
    LOG.info("*** Calling product-service with productId: {} ***", productId);
    updateProductCount(productId, entries);
    return orderDao.saveOrderIntoDB(mongoClient, orderRequest);
  }

  public void updateProductCount(long productId, MultiMap entries) {

    webClient
        .put(PRODUCT_PORT, HOSTNAME, PRODUCT_SERVICE + productId)
        .addQueryParam(PRODUCT_QUANTITY, entries.get(PRODUCT_QUANTITY))
        .addQueryParam(PRODUCT_SIZE, entries.get(PRODUCT_SIZE))
        .rxSend()
        .doOnError(
            error -> {
              LOG.error("Some error occurred while calling product-service: " + error.getMessage());
            })
        .subscribe(
            message ->
                LOG.info(
                    "Product Service responded with: {}",
                    message.bodyAsJsonObject() != null
                        ? message.bodyAsJsonObject().encodePrettily()
                        : message.statusCode()));
  }
}
