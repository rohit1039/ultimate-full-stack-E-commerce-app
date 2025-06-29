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

import com.ecommerce.orderservice.exception.ClientInputException;
import com.ecommerce.orderservice.payload.request.address.AddressRequest;
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
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the OrderDao interface responsible for handling
 * database operations related to orders.
 */
public class OrderDaoImpl implements OrderDao {

  private static final Logger LOG = LoggerFactory.getLogger(OrderDaoImpl.class.getName());

  private static final Vertx VERTX = Vertx.currentContext().owner();

  private static final WebClient WEB_CLIENT = WebClient.create(VERTX);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Saves an order into the database and processes the related payment.
   *
   * @param mongoClient the MongoClient instance used for database operations
   * @param orderRequest the details of the order to be saved
   * @param username the username of the customer placing the order
   * @param contactNumber the contact number of the customer placing the order
   * @param token the authentication token for payment processing
   * @return a future object containing the OrderResponse after successfully saving the order
   */
  @Override
  public Future<OrderResponse> saveOrder(MongoClient mongoClient, OrderRequest orderRequest,
                                         String username, String contactNumber, String token) {

    AddressRequest address = orderRequest.getAddress();
    address.setUsername(username);
    address.setPhoneNumber(contactNumber);
    address.setAddressCreationDate(LocalDateTime.now());
    address.setLastUpdatedAddressDate(LocalDateTime.now());

    return isValidAddress(mongoClient, address.getPostalCode(), address.getDistrict(), address.getStateName())
        .flatMap(valid -> {
          if (!valid) {
            return Future.failedFuture(new ClientInputException("Invalid address: "
                + "Pin does not match with district or state"));
          }
          List<OrderItemRequest> orderItems = orderRequest.getOrderItems();
          return SingleHelper.toFuture(
              toSingle(validateProducts(orderItems)).flatMap(validatedProducts -> {
                List<Integer> productIds =
                    orderItems.stream()
                              .map(OrderItemRequest::getProductId)
                              .collect(Collectors.toList());
                return Single.create(emitter -> findProductById(productIds)
                  .onSuccess(emitter::onSuccess)
                  .onFailure(emitter::onError)
                );
        }).flatMap(productResponses -> {
          List<ProductResponse> productList = objectMapper.convertValue(
              productResponses,
              new TypeReference<List<ProductResponse>>() {}
          );
          Map<Integer, Float> productPriceMap =
              productList.stream()
                         .collect(Collectors.toMap(
                             ProductResponse::getProductId,
                             ProductResponse::getTotalPrice,
                             (existing, duplicate) -> existing));
          AtomicReference<Float> totalAmountRef = new AtomicReference<>(0f);
          for (OrderItemRequest item : orderItems) {
            float price = productPriceMap.getOrDefault(item.getProductId(), 0f);
            totalAmountRef.set(totalAmountRef.get() + (price * item.getQuantity()));
          }
          JsonObject orderJson;
          try {
            orderRequest.setTotalAmount(totalAmountRef.get());
            orderRequest.setAddress(address);
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

            return Single.<PaymentResponse>create(emitter ->
                processPayment(orderId, paymentRequest, token).onComplete(ar -> {
                  if (ar.succeeded()) {
                    emitter.onSuccess(ar.result());
                  } else {
                    emitter.onError(ar.cause());
                  }
                })
            ).flatMap(paymentResponse ->
                Single.create(emitter ->
                    fetchPaymentStatusFromDb(orderId, token).onComplete(ar -> {
                      if (ar.succeeded()) {
                        emitter.onSuccess(ar.result());
                      } else {
                        emitter.onError(ar.cause());
                      }
                    })
                ).map(latestPayment -> new Object[] {
                    orderId, latestPayment, totalAmountRef.get()
                })
            );
          });
        }).flatMap(arr -> {
          String orderId = (String) arr[0];
          String paymentStatus = (String) arr[1];
          float totalAmount = (Float) arr[2];

          String newStatus = PaymentStatus.SUCCESS.name().equalsIgnoreCase(paymentStatus)
              ? OrderStatus.CONFIRMED.name()
              : OrderStatus.AWAITING_PAYMENT.name();

          JsonObject update = new JsonObject().put(SET, new JsonObject().put(ORDER_STATS, newStatus));
          JsonObject query = new JsonObject().put(ORDER_ID, orderId);

          return mongoClient.rxUpdateCollection(COLLECTION, query, update)
                .switchIfEmpty(Single.error(new IllegalStateException("Failed to update order status")))
                .flatMap(updated ->
                    mongoClient.rxFindOne(COLLECTION, query, null).toSingle()
                )
                .map(orderDoc -> {
                  OrderResponse response = new OrderResponse();
                  response.setOrderId(orderDoc.getString(ORDER_ID));
                  response.setOrderStatus(OrderStatus.valueOf(orderDoc.getString(ORDER_STATS)));
                  response.setTotalAmount(totalAmount);
                  return response;
                });
        })
    );
        });
  }

  /**
   * Retrieves a list of all orders from the MongoDB collection and enriches each order
   * with product details by fetching data using associated product IDs.
   *
   * @param mongoClient the MongoClient instance used for database operations
   * @return a Future containing a list of OrderResponseList objects, representing all
   *         retrieved and processed orders
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
   * Fetches all orders placed by a specific user identified by the username.
   *
   * @param mongoClient the MongoDB client used to interact with the database
   * @param username the username of the user whose orders need to be retrieved
   * @return a Future containing a list of OrderResponseList objects representing
   *         the user's orders with detailed information including order items and products
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
   * Updates the status of an existing order in the database.
   *
   * @param mongoClient the MongoClient instance used for database operations.
   * @param orderId the unique identifier of the order to be updated.
   * @param orderStatus the new status to be set for the order.
   * @return a Future containing an OrderResponse object with the updated order details,
   *         or an error if the update fails or the order is not found in the database.
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

  /**
   * Updates the order statistics for a specific order in the database.
   * The method updates the order's payment status, payment method, and order status
   * based on the information provided and fetches the updated order details.
   *
   * @param mongoClient an instance of {@link MongoClient} for interacting with the MongoDB database.
   * @param orderId the unique identifier of the order that needs to be updated.
   * @param paymentStatus the payment status string representing the current state of the payment
   *                      (e.g., SUCCESS, FAILED).
   * @param paymentMethod the payment method used for the order, such as Credit Card, PayPal, etc.
   * @return a {@link Future} containing the updated {@link OrderResponse} with the latest order details.
   */
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
   * Validates a list of order items by invoking the product-service.
   * Ensures that the provided order items meet the necessary criteria.
   *
   * @param orderItems the list of {@link OrderItemRequest} to be validated
   * @return a {@link Future} containing the validated list of {@link OrderItemRequest} if successful,
   *         or a failure if validation fails
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

  /**
   * Processes a payment for a specific order by communicating with the payment service.
   * Sends a payment request to the payment service and retrieves a payment response.
   * If the payment is successful, the future is completed with the payment response,
   * otherwise, it fails with the appropriate error details.
   *
   * @param orderId the unique identifier of the order for which payment is to be processed
   * @param paymentRequest the payment request containing details such as the total amount and payment date
   * @param token the authorization token used for accessing the payment service
   * @return a future representing the asynchronous result of the payment processing;
   *         contains a {@link PaymentResponse} if successful, otherwise the future fails
   */
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
   * Fetches the payment status for a specific order from the payment service
   * and logs the result. If successful, it resolves with the payment status.
   * Otherwise, it resolves with an appropriate failure message in case of an error.
   *
   * @param orderId The unique identifier of the order whose payment status needs to be fetched.
   * @param token The authorization token required to access the payment service.
   * @return A Future that resolves with the payment status on success, or an error message on failure.
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
   * Fetches product details for the given list of product IDs by making calls to an external service.
   *
   * @param productIds a list of product IDs for which the product details need to be fetched
   * @return a Future containing a list of {@link ProductResponse} objects representing the product details
   *         for the requested product IDs; failure in fetching any ID results in a failed Future
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
   * Converts a {@code Future} into a {@code Single}. This method bridges Vert.x {@code Future}
   * with RxJava {@code Single} for reactive programming.
   *
   * @param future the {@code Future} to be converted into a {@code Single}
   * @param <T> the type of the result contained in the {@code Future} and {@code Single}
   * @return a {@code Single} that represents the result of the given {@code Future},
   *         emitting success when the future succeeds or an error when the future fails
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

  /**
   * Validates whether the given address details (pincode, district, state) exist in the database.
   *
   * @param mongoClient the MongoClient instance to interact with the database
   * @param pincode the postal code of the address to be validated
   * @param district the district name of the address to be validated
   * @param state the state name of the address to be validated
   * @return a Future containing a Boolean value: true if the address is valid, or false otherwise
   */
  public Future<Boolean> isValidAddress(MongoClient mongoClient, Long pincode,
                               String district, String state) {

    JsonObject query = new JsonObject()
        .put("pincode", pincode)
        .put("district",
            new JsonObject().put("$regex", "^" + Pattern.quote(district) + "$").put("$options", "i"))
        .put("state",
            new JsonObject().put("$regex", "^" + Pattern.quote(state) + "$").put("$options", "i"));

    return SingleHelper.toFuture(
        mongoClient.rxFindOne("pincode_data", query, null)
         .map(Objects::nonNull)
         .defaultIfEmpty(false)
         .onErrorReturn(err -> {
           LOG.error("Error in address validation: " + err.getMessage());
           return false;
         }));
  }
}
