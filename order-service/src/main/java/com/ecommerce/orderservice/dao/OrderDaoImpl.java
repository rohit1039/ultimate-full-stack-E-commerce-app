package com.ecommerce.orderservice.dao;

import static com.ecommerce.orderservice.constant.APIConstants.*;

import com.ecommerce.orderservice.payload.request.order.OrderItemRequest;
import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import io.vertx.core.Future;
import io.vertx.rxjava3.core.Promise;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.client.WebClient;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderDaoImpl implements OrderDao {

  private static final Logger LOG = LoggerFactory.getLogger(OrderDaoImpl.class.getName());

  private static final Vertx VERTX = Vertx.currentContext().owner();

  private static final WebClient WEB_CLIENT = WebClient.create(VERTX);

  /**
   * Saves an order in the database.
   *
   * @param mongoClient the MongoDB client
   * @param routingContext the routing context
   * @param orderRequest the order request
   * @return a Future that will complete with the order response
   */
  @Override
  public Future<OrderResponse> saveOrderInDb(
      MongoClient mongoClient, RoutingContext routingContext, OrderRequest orderRequest) {

    List<OrderItemRequest> orderItems = orderRequest.getOrderItemList();

    return callToProductService(orderItems)
        .compose(
            orders -> {
              Promise<OrderResponse> promise = Promise.promise();
              mongoClient
                  .rxSave(COLLECTION_NAME, OrderRequest.toJson(orderRequest))
                  .doFinally(mongoClient::close)
                  .subscribe(
                      orderId -> promise.complete(OrderResponse.builder().orderId(orderId).build()),
                      error -> {
                        LOG.error(
                            "Some error occurred while saving order into database: {}",
                            error.getMessage());
                        promise.fail("Unable to save order into database");
                      });
              return promise.future();
            });
  }

  /**
   * Calls the product service to validate the order items.
   *
   * @param orderItems the list of order items
   * @return a Future that will complete with the list of successfully validated order items
   */
  private Future<List<OrderItemRequest>> callToProductService(List<OrderItemRequest> orderItems) {

    Promise<Void> promise = Promise.promise();
    LOG.info("*** Calling product-service to validate and place the order ***");
    WEB_CLIENT
        .put(PRODUCT_PORT, PRODUCT_HOSTNAME, PRODUCT_ENDPOINT)
        .rxSendJson(orderItems)
        .subscribe(
            ar -> {
              if (ar.statusCode() == SUCCESS_STATUS_CODE) {
                promise.complete();
              } else {
                LOG.error("Some error occurred in product-service");
                promise.fail(ar.bodyAsString());
              }
            });

    return promise.future().map(f -> orderItems);
  }
}
