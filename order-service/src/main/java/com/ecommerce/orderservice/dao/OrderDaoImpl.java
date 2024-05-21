package com.ecommerce.orderservice.dao;

import static com.ecommerce.orderservice.constant.APIConstants.*;
import static io.vertx.core.Future.future;

import com.ecommerce.orderservice.exception.GlobalException;
import com.ecommerce.orderservice.payload.request.order.OrderItemRequest;
import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import io.vertx.core.Future;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.client.WebClient;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderDaoImpl implements OrderDao {

  private static final Logger LOG = LoggerFactory.getLogger(OrderDaoImpl.class.getName());

  private static final Vertx VERTX = Vertx.currentContext().owner();

  private static final WebClient WEB_CLIENT = WebClient.create(VERTX);

  @Override
  public Future<OrderResponse> saveOrderInDb(
      MongoClient mongoClient, RoutingContext routingContext, OrderRequest orderRequest) {

    List<OrderItemRequest> orderItems = orderRequest.getOrderItemList();

    LOG.info("*** Calling product-service to check if selected products is/are in stock ***");
    List<OrderItemRequest> successOrderItems = callToProductService(orderItems);

    return future(
        promise -> {
          if (successOrderItems.size() > 0) {
            mongoClient
                .rxSave(COLLECTION_NAME, OrderRequest.toJson(orderRequest))
                .doFinally(mongoClient::rxClose)
                .subscribe(
                    orderId -> {
                      promise.complete(OrderResponse.builder().orderId(orderId).build());
                    },
                    error -> {
                      LOG.error(
                          "Some error occurred while saving order into database: {}",
                          error.getMessage());
                      promise.fail("Unable to save order into database");
                    });
          } else {
            throw new GlobalException(
                "Unable to place your orders, as selected products not in stock");
          }
        });
  }

  private List<OrderItemRequest> callToProductService(List<OrderItemRequest> orderItems) {

    List<OrderItemRequest> successOrderItems = new ArrayList<>();

    orderItems.forEach(
        orderItem ->
            WEB_CLIENT
                .put(PRODUCT_PORT, PRODUCT_HOSTNAME, PRODUCT_ENDPOINT + orderItem.getProductId())
                .addQueryParam(PRODUCT_QUANTITY, String.valueOf(orderItem.getQuantity()))
                .addQueryParam(PRODUCT_SIZE, orderItem.getProductSize())
                .rxSend()
                .subscribe(
                    productRes -> {
                      if (productRes.statusCode() == SUCCESS_STATUS_CODE) {
                        successOrderItems.add(orderItem);
                      }
                    }));

    return successOrderItems;
  }
}
