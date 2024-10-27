package com.ecommerce.orderservice.dao;

import static com.ecommerce.orderservice.constant.ApiConstants.ADDRESS;
import static com.ecommerce.orderservice.constant.ApiConstants.COLLECTION;
import static com.ecommerce.orderservice.constant.ApiConstants.HOST;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_ID;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_ITEMS;
import static com.ecommerce.orderservice.constant.ApiConstants.PAYMENT;
import static com.ecommerce.orderservice.constant.ApiConstants.PORT;
import static com.ecommerce.orderservice.constant.ApiConstants.PRODUCT_BY_ID_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.PRODUCT_ID;
import static com.ecommerce.orderservice.constant.ApiConstants.PRODUCT_ORDER_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.SUCCESS_STATUS_CODE;
import static com.ecommerce.orderservice.constant.ApiConstants.USERNAME;

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
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Promise;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.client.WebClient;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
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
   * @param mongoClient    the MongoDB client
   * @param routingContext the routing context
   * @param orderRequest   the order request
   * @return a Future that will complete with the order response
   */
  @Override
  public Future<OrderResponse> saveOrderInDb(MongoClient mongoClient, RoutingContext routingContext,
                                             OrderRequest orderRequest) {

    List<OrderItemRequest> orderItems = orderRequest.getOrderItems();
    return callToProductService(orderItems).flatMap(orders -> {
      Promise<OrderResponse> promise = Promise.promise();
      mongoClient.rxSave(COLLECTION, OrderRequest.toJson(orderRequest))
                 .doFinally(mongoClient::close)
                 .subscribe(
                     orderId -> promise.complete(OrderResponse.builder().orderId(orderId).build()),
                     error -> {
                       LOG.error("Some error occurred while saving order into database: {}",
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
   * @param username    the username to find orders for
   * @return a Future that will complete with a list of orders
   */
  public Future<List<OrderResponseList>> getOrdersFromDb(MongoClient mongoClient, String username) {

    Promise<List<OrderResponseList>> promise = Promise.promise();
    AtomicReference<List<Integer>> productIds = new AtomicReference<>(new CopyOnWriteArrayList<>());
    AtomicReference<List<OrderResponseList>> orderList =
        new AtomicReference<>(new CopyOnWriteArrayList<>());
    mongoClient.find(COLLECTION, new JsonObject().put(USERNAME, username)).flatMap(res -> {
      orderList.set(res.stream().map(orderRes -> {
        productIds.set(orderRes.getJsonArray(ORDER_ITEMS)
                               .stream()
                               .map(item -> ((JsonObject) item).getInteger(PRODUCT_ID))
                               .collect(Collectors.toList()));
        return OrderResponseList.builder()
                                .orderId(orderRes.getString(ORDER_ID))
                                .username(orderRes.getString(USERNAME))
                                .orderItems(orderRes.getJsonArray(ORDER_ITEMS))
                                .transactionDetails(
                                    orderRes.getJsonObject(PAYMENT).mapTo(PaymentRequest.class))
                                .shippingAddress(
                                    orderRes.getJsonObject(ADDRESS).mapTo(AddressRequest.class))
                                .build();
      }).collect(Collectors.toList()));
      return Single.create(
          emitter -> findProductById(productIds.get()).onSuccess(emitter::onSuccess)
                                                      .onFailure(emitter::onError));
    }).flatMap(productResponses -> Single.just(orderList.get().stream().peek(order -> {
      order.setProducts(
          objectMapper.convertValue(productResponses, new TypeReference<List<ProductResponse>>() {
          }));
    }).collect(Collectors.toList()))).subscribe(promise::complete, promise::fail);
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
    WEB_CLIENT.put(PORT, HOST, PRODUCT_ORDER_ENDPOINT).rxSendJson(orderItems).subscribe(ar -> {
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
    productIds.forEach(productId -> {
      Future<ProductResponse> future = Future.future(promiseHandler -> {
        WEB_CLIENT.get(PORT, HOST, PRODUCT_BY_ID_ENDPOINT + productId).rxSend().subscribe(res -> {
          if (res.statusCode() == SUCCESS_STATUS_CODE) {
            promiseHandler.complete(res.bodyAsJsonObject().mapTo(ProductResponse.class));
          } else {
            promiseHandler.fail("Failed to fetch product with id: " + productId);
          }
        }, error -> promiseHandler.fail(error.getMessage()));
      });
      futures.add(future);
    });
    Future.join(futures).onSuccess(composite -> {
      List<ProductResponse> productResponses = composite.list();
      promise.complete(productResponses);
    }).onFailure(promise::fail);
    return promise.future();
  }
}
