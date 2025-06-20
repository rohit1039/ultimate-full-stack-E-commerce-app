package com.ecommerce.orderservice.dao;

import static com.ecommerce.orderservice.constant.ApiConstants.AUTH_HEADER;
import static com.ecommerce.orderservice.constant.ApiConstants.COLLECTION;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_ID;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_ITEMS;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_PLACED_AT;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_PLACED_BY;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_STATS;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_UPDATED_AT;
import static com.ecommerce.orderservice.constant.ApiConstants.PAYMENT_HOST;
import static com.ecommerce.orderservice.constant.ApiConstants.PAYMENT_METHOD;
import static com.ecommerce.orderservice.constant.ApiConstants.PAYMENT_ORDER_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.PAYMENT_STATUS_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.PORT;
import static com.ecommerce.orderservice.constant.ApiConstants.PRODUCT_BY_ID_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.PRODUCT_HOST;
import static com.ecommerce.orderservice.constant.ApiConstants.PRODUCT_ID;
import static com.ecommerce.orderservice.constant.ApiConstants.PRODUCT_ORDER_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.SET;
import static com.ecommerce.orderservice.constant.ApiConstants.SUCCESS_STATUS_CODE;

import com.ecommerce.orderservice.payload.request.order.OrderItemRequest;
import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import com.ecommerce.orderservice.payload.request.order.OrderStatus;
import com.ecommerce.orderservice.payload.request.payment.PaymentRequest;
import com.ecommerce.orderservice.payload.request.payment.PaymentStatus;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import com.ecommerce.orderservice.payload.response.OrderResponseList;
import com.ecommerce.orderservice.payload.response.PaymentResponse;
import com.ecommerce.orderservice.payload.response.ProductResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.SingleHelper;
import io.vertx.rxjava3.core.Promise;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import io.vertx.rxjava3.ext.web.client.WebClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
   * @param mongoClient the MongoDB client
   * @param orderRequest the order request containing details of the order to be saved
   * @return a Future that will complete with the order response
   */
  @Override
  public Future<OrderResponse> saveOrder(MongoClient mongoClient, OrderRequest orderRequest, String token) {

    List<OrderItemRequest> orderItems = orderRequest.getOrderItems();

    return SingleHelper.toFuture(toSingle(validateProducts(orderItems)).flatMap(validatedProducts -> {
      List<Integer> productIds =
          orderItems.stream().map(OrderItemRequest::getProductId).collect(Collectors.toList());

      return Single.create(emitter ->
          findProductById(productIds)
              .onSuccess(emitter::onSuccess)
              .onFailure(emitter::onError));

    }).flatMap(productResponses -> {

      List<ProductResponse> productList =
          objectMapper.convertValue(productResponses, new TypeReference<List<ProductResponse>>() {});

      Map<Integer, Float> productPriceMap =
          productList.stream()
                     .collect(Collectors.toMap(ProductResponse::getProductId, ProductResponse::getTotalPrice,
                         (existing, duplicate) -> existing));

      AtomicReference<Float> totalAmountRef = new AtomicReference<>(0f);
      for (OrderItemRequest item : orderItems) {
        float price = productPriceMap.getOrDefault(item.getProductId(), 0f);
        totalAmountRef.set(totalAmountRef.get() + (price * item.getQuantity()));
      }

      JsonObject orderJson;
      try {
        orderRequest.setTotalAmount(totalAmountRef.get());
        orderJson = OrderRequest.toJson(orderRequest);
      } catch (JsonProcessingException e) {
        LOG.error("JSON processing failed: {}", e.getMessage());
        return Single.error(new RuntimeException("Failed to process order data"));
      }

      return mongoClient.rxSave(COLLECTION, orderJson)
           .switchIfEmpty(Single.error(new IllegalStateException("Failed to save order")))
           .flatMap(orderId -> {
             LOG.info("processPayment called with orderId :: " + orderId);

             PaymentRequest paymentRequest = new PaymentRequest();
             paymentRequest.setPaymentDate(LocalDateTime.now());
             paymentRequest.setTotalAmount(totalAmountRef.get());

             return Single.<PaymentResponse>create(
                  emitter -> processPayment(orderId, paymentRequest, token).onComplete(ar -> {
                    if (ar.succeeded()) {
                      emitter.onSuccess(ar.result());
                    } else {
                      emitter.onError(ar.cause());
                    }
                  })).flatMap(paymentResponse -> Single.create(
                      emitter -> fetchPaymentStatusFromDb(orderId, token).onComplete(ar -> {
                        if (ar.succeeded()) {
                          emitter.onSuccess(ar.result());
                        } else {
                          emitter.onError(ar.cause());
                        }
                      })
             ).map(latestPayment -> new Object[] { orderId, latestPayment, totalAmountRef.get() }));
           });
    }).flatMap(arr -> {

      String orderId = (String) arr[0];
      String paymentStatus = (String) arr[1];
      float totalAmount = (Float) arr[2];

      String newStatus = PaymentStatus.SUCCESS.name().equalsIgnoreCase(paymentStatus)
          ? OrderStatus.CONFIRMED.name() : OrderStatus.AWAITING_PAYMENT.name();

      JsonObject update = new JsonObject().put(SET, new JsonObject().put(ORDER_STATS, newStatus));
      JsonObject query = new JsonObject().put(ORDER_ID, orderId);

      return mongoClient.rxUpdateCollection(COLLECTION, query, update)
             .switchIfEmpty(Single.error(new IllegalStateException("Failed to update order status")))
             .flatMap(updated -> mongoClient.rxFindOne(COLLECTION, query, null).toSingle())
             .map(orderDoc -> {
               OrderResponse response = new OrderResponse();
               response.setOrderId(orderDoc.getString(ORDER_ID));
               response.setOrderStatus(OrderStatus.valueOf(orderDoc.getString(ORDER_STATS)));
               response.setTotalAmount(totalAmount);
               return response;
             });
    }));
  }

  /**
   * Retrieves all orders from the database.
   *
   * @param mongoClient the MongoDB client
   * @return a Future that will complete with a list of order response objects
   */
  @Override
  public Future<List<OrderResponseList>> getAllOrders(MongoClient mongoClient) {

    final Promise<List<OrderResponseList>> promise = Promise.promise();

    AtomicReference<List<OrderResponseList>> orders = new AtomicReference<>(new ArrayList<>());

    mongoClient.find(COLLECTION, new JsonObject()).flatMap(res -> {

      List<Integer> allProductIds =
          res.stream()
             .flatMap(orderRes -> orderRes.getJsonArray(ORDER_ITEMS).stream())
             .map(item -> ((JsonObject) item).getInteger(PRODUCT_ID))
             .collect(Collectors.toList());

      orders.set(res.stream().map(orderRes -> {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");
        LocalDateTime orderDate = LocalDateTime.parse(orderRes.getString(ORDER_PLACED_AT), formatter);
        return OrderResponseList.builder()
               .orderId(orderRes.getString(ORDER_ID))
               .orderStatus(OrderStatus.valueOf(orderRes.getString(ORDER_STATS)))
               .username(orderRes.getString(ORDER_PLACED_BY))
               .orderItems(orderRes.getJsonArray(ORDER_ITEMS))
               .orderDate(orderDate)
               .build();
      }).collect(Collectors.toList()));

      return Single.create(emitter ->
          findProductById(allProductIds).onSuccess(emitter::onSuccess).onFailure(emitter::onError));

    }).flatMap(productResponses -> Single.just(orders.get().stream().peek(order -> {
      List<ProductResponse> productList = objectMapper.convertValue(productResponses,
          new TypeReference<List<ProductResponse>>() {});
      JsonArray jsonArray = order.getOrderItems();
      List<OrderItemRequest> orderItems = new ArrayList<>();
      for (int i = 0; i < jsonArray.size(); i++) {
        JsonObject jsonObject = jsonArray.getJsonObject(i);
        try {
          OrderItemRequest itemRequest =
              objectMapper.treeToValue(objectMapper.readTree(jsonObject.encode()),
                  OrderItemRequest.class);
          orderItems.add(itemRequest);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }
      Map<Integer, ProductResponse> productMap =
          productList.stream().collect(Collectors.toMap(ProductResponse::getProductId,
              product -> product, (existing, replacement) -> existing));

      List<ProductResponse> matchingProducts = new ArrayList<>();

      orderItems.forEach(orderItem -> {
        ProductResponse product = productMap.get(orderItem.getProductId());
        matchingProducts.add(product);
      });

      order.setProducts(matchingProducts);

    }).sorted(Comparator.comparing(OrderResponseList::getOrderId)
                        .reversed()).collect(Collectors.toList())))
               .doFinally(mongoClient::close)
               .subscribe(promise::complete, promise::fail);

    return promise.future();
  }

  /**
   * Retrieves orders by username.
   *
   * @param mongoClient the MongoDB client
   * @param username the username whose orders need to be fetched
   * @return a Future that will complete with a list of order response objects
   */
  @Override
  public Future<List<OrderResponseList>> getOrdersByUsername(MongoClient mongoClient,
                                                             String username) {

    final Promise<List<OrderResponseList>> promise = Promise.promise();

    AtomicReference<List<OrderResponseList>> orders = new AtomicReference<>(new ArrayList<>());

    mongoClient.find(COLLECTION, new JsonObject().put(ORDER_PLACED_BY, username)).flatMap(res -> {

      List<Integer> allProductIds =
          res.stream()
             .flatMap(orderRes -> orderRes.getJsonArray(ORDER_ITEMS).stream())
             .map(item -> ((JsonObject) item).getInteger(PRODUCT_ID))
             .collect(Collectors.toList());

      orders.set(res.stream().map(orderRes -> {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");
        LocalDateTime orderDate = LocalDateTime.parse(orderRes.getString(ORDER_PLACED_AT), formatter);
        return OrderResponseList.builder()
               .orderId(orderRes.getString(ORDER_ID))
               .orderStatus(OrderStatus.valueOf(orderRes.getString(ORDER_STATS)))
               .username(orderRes.getString(ORDER_PLACED_BY))
               .orderItems(orderRes.getJsonArray(ORDER_ITEMS))
               .orderDate(orderDate).build();
      }).collect(Collectors.toList()));

      return Single.create(emitter ->
          findProductById(allProductIds).onSuccess(emitter::onSuccess).onFailure(emitter::onError));

    }).flatMap(productResponses -> Single.just(orders.get().stream().peek(order -> {
      List<ProductResponse> productList = objectMapper.convertValue(productResponses,
          new TypeReference<List<ProductResponse>>() {});

      JsonArray jsonArray = order.getOrderItems();
      List<OrderItemRequest> orderItems = new ArrayList<>();
      for (int i = 0; i < jsonArray.size(); i++) {
        JsonObject jsonObject = jsonArray.getJsonObject(i);
        try {
          OrderItemRequest itemRequest =
              objectMapper.treeToValue(objectMapper.readTree(jsonObject.encode()),
                  OrderItemRequest.class);
          orderItems.add(itemRequest);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }
      Map<Integer, ProductResponse> productMap =
          productList.stream().collect(Collectors.toMap(ProductResponse::getProductId,
              product -> product, (existing, replacement) -> existing));

      List<ProductResponse> matchingProducts = new ArrayList<>();
      orderItems.forEach(orderItem -> {
        ProductResponse product = productMap.get(orderItem.getProductId());
        matchingProducts.add(product);
      });

      order.setProducts(matchingProducts);

    }).sorted(Comparator.comparing(OrderResponseList::getOrderId)
                        .reversed()).collect(Collectors.toList())))
               .doFinally(mongoClient::close)
               .subscribe(promise::complete, promise::fail);

    return promise.future();

  }

  /**
   * Update order by orderId.
   *
   * @param mongoClient the MongoDB client
   * @param orderId id of the order to update
   * @param orderStatus the updated order status value
   * @return a Future that will complete with the order response
   */
  @Override
  public Future<OrderResponse> updateOrder(MongoClient mongoClient, String orderId,
                                           String orderStatus) {

    Promise<OrderResponse> promise = Promise.promise();

    JsonObject query = new JsonObject().put(ORDER_ID, orderId);

    mongoClient
        .rxFindOne(COLLECTION, query, null)
        .switchIfEmpty(Single.error(
            new NoSuchElementException("Order not found with orderId: " + orderId)))
        .flatMap(doc -> {
          JsonObject updateFields = new JsonObject()
              .put(ORDER_STATS, OrderStatus.valueOf(orderStatus))
              .put(ORDER_UPDATED_AT, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a")));
          JsonObject update = new JsonObject().put(SET, updateFields);
          return mongoClient.rxUpdateCollection(COLLECTION, query, update).toSingle();
        })
        .flatMap(updateResult ->
            mongoClient.rxFindOne(COLLECTION, query, null)
            .switchIfEmpty(Single.error(
                new IllegalStateException("Updated order with orderId: "
                    + orderId + "not found"))))
        .subscribe(updatedDoc -> {
          OrderResponse response = new OrderResponse();
          response.setOrderId(updatedDoc.getString(ORDER_ID));
          response.setOrderStatus(OrderStatus.valueOf(updatedDoc.getString(ORDER_STATS)));
          promise.complete(response);
          }, error -> {
            LOG.error("Failed to update order: {}", error.getMessage());
            promise.fail(error);
          });

    return promise.future();
  }

  @Override
  public Future<OrderResponse> updateOrderStats(MongoClient mongoClient, String orderId,
                                                String paymentStatus, String paymentMethod) {

    Promise<OrderResponse> promise = Promise.promise();

    JsonObject query = new JsonObject().put(ORDER_ID, orderId);

    String finalStatus = PaymentStatus.SUCCESS.name().equalsIgnoreCase(paymentStatus)
        ? OrderStatus.CONFIRMED.name() : OrderStatus.PAYMENT_FAILED.name();

    mongoClient
        .rxFindOne(COLLECTION, query, null)
        .switchIfEmpty(Single.error(
          new NoSuchElementException("Order not found with orderId: " + orderId)))
        .flatMap(doc -> {
          JsonObject updateFields = new JsonObject()
              .put(ORDER_STATS, finalStatus)
              .put(ORDER_UPDATED_AT, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a")))
              .put(PAYMENT_METHOD, paymentMethod);
          JsonObject update = new JsonObject().put(SET, updateFields);
          return mongoClient.updateCollection(COLLECTION, query, update).toSingle();
        })
        .flatMap(updateResult -> mongoClient.rxFindOne(COLLECTION, query, null)
              .switchIfEmpty(Single.error(
                  new IllegalStateException("Updated order with orderId: " + orderId + "not found"))))
        .subscribe(updatedDoc -> {
          JsonArray itemArray = updatedDoc.getJsonArray("order_items", new JsonArray());
          List<OrderItemRequest> items =
              itemArray.stream()
                       .map(obj -> Json.decodeValue(obj.toString(), OrderItemRequest.class))
                       .collect(Collectors.toList());
          OrderResponse response = new OrderResponse();
          response.setOrderId(updatedDoc.getString(ORDER_ID));
          response.setOrderStatus(OrderStatus.valueOf(updatedDoc.getString(ORDER_STATS)));
          response.setOrderItems(items);
          promise.complete(response);
          }, error -> {
            LOG.error("Failed to update order: {}", error.getMessage());
            promise.fail(error);
          });
    return promise.future();
  }

  /**
   * Calls the product service to validate the order items.
   *
   * @param orderItems the list of order items
   * @return a Future that will complete with the list of successfully validated order items
   */
  private Future<List<OrderItemRequest>> validateProducts(List<OrderItemRequest> orderItems) {

    Promise<List<OrderItemRequest>> promise = Promise.promise();
    LOG.info("*** Calling product-service to validate the products ***");

    WEB_CLIENT.put(PORT, PRODUCT_HOST, PRODUCT_ORDER_ENDPOINT).rxSendJson(orderItems).subscribe(ar -> {
      if (ar.statusCode() == SUCCESS_STATUS_CODE) {
        promise.complete(orderItems);
      } else {
        LOG.error("Some error occurred in product-service: {}", ar.bodyAsString());
        promise.fail(ar.bodyAsString());
      }
    }, throwable -> {
      LOG.error("Request to product-service failed", throwable);
      promise.fail(throwable);
    });

    return promise.future();
  }

  private Future<PaymentResponse> processPayment(String orderId, PaymentRequest paymentRequest, String token) {

    Promise<PaymentResponse> promise = Promise.promise();
    LOG.info("*** Calling payment-service to do the payment ***");

    WEB_CLIENT.post(PORT, PAYMENT_HOST, PAYMENT_ORDER_ENDPOINT + orderId).putHeader(AUTH_HEADER, token)
    .rxSendJson(paymentRequest).subscribe(ar -> {
      if (ar.statusCode() == SUCCESS_STATUS_CODE) {
        PaymentResponse paymentResponse = ar.bodyAsJsonObject().mapTo(PaymentResponse.class);
        promise.complete(paymentResponse);
      } else {
        LOG.error("Some error occurred in payment-service for orderId: {}", orderId);
        promise.fail(ar.bodyAsString());
      }
    }, throwable -> {
      LOG.error("Request to payment-service failed", throwable);
      promise.fail(throwable);
    });

    return promise.future();
  }

  /**
   * Fetch latest payment status from payment DB
   * @param orderId - fetch payment status by orderId
   * @param token - JWT
   * @return paymentResponse with updated status
   */
  private Future<String> fetchPaymentStatusFromDb(String orderId, String token) {
    Promise<String> promise = Promise.promise();
    WEB_CLIENT.get(PORT, PAYMENT_HOST, PAYMENT_STATUS_ENDPOINT + orderId).putHeader(AUTH_HEADER, token)
    .rxSend().subscribe(response -> {
      if (response.statusCode() == SUCCESS_STATUS_CODE) {
        String status = response.bodyAsString();
        LOG.info("Received payment status: {}", status);
        promise.complete(status);
      } else {
        LOG.error("Error from payment service: {}", response.bodyAsString());
        promise.fail("Failed to fetch status");
      }
    }, err -> {
      LOG.error("Exception while fetching status", err);
      promise.fail(err);
    });
    return promise.future();
  }

  /**
   * Asynchronously fetches a list of products by their IDs.
   *
   * <p>This method performs the following steps:
   * <ul>
   *   <li>Iterates over the provided product IDs.</li>
   *   <li>For each product ID, sends an asynchronous HTTP GET request to fetch the product details.
   *   </li>
   *   <li>Handles the response and maps it to {@link ProductResponse} objects.</li>
   *   <li>Combines all individual product fetch results into a single list.</li>
   * </ul>
   *
   * @param productIds a list of product IDs to fetch details for.
   * @return a {@link Future} that completes with a list of {@link ProductResponse}
   * objects when all product fetches succeed, or fails if any of the fetches fail.
   */
  private Future<List<ProductResponse>> findProductById(List<Integer> productIds) {

    Promise<List<ProductResponse>> promise = Promise.promise();
    List<Future<ProductResponse>> futures = new CopyOnWriteArrayList<>();
    productIds.forEach(productId -> {
      Future<ProductResponse> future = Future.future(promiseHandler -> {
        WEB_CLIENT.get(PORT, PRODUCT_HOST, PRODUCT_BY_ID_ENDPOINT + productId).rxSend().subscribe(res -> {
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

  /**
   * This is a helper method to convert Vert.x Future to Single
   */
  public static <T> Single<T> toSingle(Future<T> future) {
    return Single.create(emitter ->
        future.onComplete(ar -> {
          if (ar.succeeded()) {
            emitter.onSuccess(ar.result());
          } else {
            emitter.onError(ar.cause());
          }
        })
    );
  }
}
