package com.ecommerce.orderservice.dao;

import static com.ecommerce.orderservice.constant.APIConstants.*;

import com.ecommerce.orderservice.payload.request.address.AddressRequest;
import com.ecommerce.orderservice.payload.request.order.OrderItemRequest;
import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import com.ecommerce.orderservice.payload.request.payment.PaymentRequest;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import com.ecommerce.orderservice.payload.response.OrderResponseList;
import com.ecommerce.orderservice.payload.response.ProductResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Promise;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.client.WebClient;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderDaoImpl implements OrderDao {

  private static final Logger LOG = LoggerFactory.getLogger(OrderDaoImpl.class.getName());

  private static final Vertx VERTX = Vertx.currentContext().owner();

  private static final WebClient WEB_CLIENT = WebClient.create(VERTX);

  private static final ObjectMapper objectMapper = new ObjectMapper();

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

    List<OrderItemRequest> orderItems = orderRequest.getOrderItems();

    return callToProductService(orderItems)
        .flatMap(
            orders -> {
              Promise<OrderResponse> promise = Promise.promise();
              mongoClient
                  .rxSave(COLLECTION, OrderRequest.toJson(orderRequest))
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
   * Retrieves orders for a specific username from the database.
   *
   * @param mongoClient the MongoDB client
   * @param username the username to find orders for
   * @return a Future that will complete with a list of orders
   */
  public Future<List<OrderResponseList>> getOrdersFromDb(MongoClient mongoClient, String username) {

    List<OrderResponseList> orderList = new CopyOnWriteArrayList<>();
    List<Integer> productIds = new CopyOnWriteArrayList<>();
    Promise<List<OrderResponseList>> promise = Promise.promise();

    mongoClient
        .find(COLLECTION, new JsonObject().put(USERNAME, username))
        .flatMap(
            res -> {
              for (JsonObject orderRes : res) {
                JsonArray orderItems = orderRes.getJsonArray(ORDER_ITEMS);

                for (int j = 0; j < orderItems.size(); j++) {
                  int productId = orderItems.getJsonObject(j).getInteger(PRODUCT_ID);
                  productIds.add(productId);
                }

                OrderResponseList orderResponse =
                    OrderResponseList.builder()
                        .orderId(orderRes.getString("_id"))
                        .username(orderRes.getString(USERNAME))
                        .orderItems(orderRes.getJsonArray(ORDER_ITEMS))
                        .transactionDetails(
                            orderRes.getJsonObject(PAYMENT).mapTo(PaymentRequest.class))
                        .shippingAddress(
                            orderRes.getJsonObject(ADDRESS).mapTo(AddressRequest.class))
                        .build();

                orderList.add(orderResponse);
              }

              // Convert Future to Single
              return Single.create(
                  emitter -> {
                    findProductById(productIds)
                        .onSuccess(emitter::onSuccess)
                        .onFailure(emitter::onError);
                  });
            })
        .flatMap(
            productResponses -> {
              // Set product responses in the order list
              List<OrderResponseList> updatedOrderList =
                  orderList.stream()
                      .map(
                          order -> {
                            order.setProducts(
                                objectMapper.convertValue(
                                    productResponses,
                                    new TypeReference<List<ProductResponse>>() {}));
                            return order;
                          })
                      .collect(Collectors.toList());

              return Single.just(updatedOrderList);
            })
        .subscribe(promise::complete, promise::fail);

    return promise.future();
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

  private Future<List<ProductResponse>> findProductById(List<Integer> productIds) {

    Promise<List<ProductResponse>> promise = Promise.promise();
    List<Future<ProductResponse>> futures = new CopyOnWriteArrayList<>();

    productIds.forEach(
        productId -> {
          Future<ProductResponse> future =
              Future.future(
                  promiseHandler -> {
                    WEB_CLIENT
                        .get(8081, PRODUCT_HOSTNAME, "/products/v1/get/" + productId)
                        .rxSend()
                        .subscribe(
                            res -> {
                              if (res.statusCode() == SUCCESS_STATUS_CODE) {
                                promiseHandler.complete(
                                    res.bodyAsJsonObject().mapTo(ProductResponse.class));
                              } else {
                                promiseHandler.fail(
                                    "Failed to fetch product with id: " + productId);
                              }
                            },
                            error -> promiseHandler.fail(error.getMessage()));
                  });

          futures.add(future);
        });

    Future.join(futures)
        .onSuccess(
            composite -> {
              List<ProductResponse> productResponses = composite.list();
              promise.complete(productResponses);
            })
        .onFailure(promise::fail);

    return promise.future();
  }
}
