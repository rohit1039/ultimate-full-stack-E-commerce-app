package com.ecommerce.orderservice.dao;

import static com.ecommerce.orderservice.constant.ApiConstants.COLLECTION;
import static com.ecommerce.orderservice.constant.ApiConstants.HOST;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_ID;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_ITEMS;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_PLACED_AT;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_PLACED_BY;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_STATS;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_UPDATED_AT;
import static com.ecommerce.orderservice.constant.ApiConstants.PORT;
import static com.ecommerce.orderservice.constant.ApiConstants.PRODUCT_BY_ID_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.PRODUCT_ID;
import static com.ecommerce.orderservice.constant.ApiConstants.PRODUCT_ORDER_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.SET;
import static com.ecommerce.orderservice.constant.ApiConstants.SUCCESS_STATUS_CODE;

import com.ecommerce.orderservice.payload.request.order.OrderItemRequest;
import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import com.ecommerce.orderservice.payload.request.order.OrderStatus;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import com.ecommerce.orderservice.payload.response.OrderResponseList;
import com.ecommerce.orderservice.payload.response.ProductResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
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
  public Future<OrderResponse> saveOrder(MongoClient mongoClient,
                                         OrderRequest orderRequest) {

    List<OrderItemRequest> orderItems = orderRequest.getOrderItems();
    return callToProductService(orderItems).flatMap(orders -> {
      Promise<OrderResponse> promise = Promise.promise();
      try {
        mongoClient.rxSave(COLLECTION, OrderRequest.toJson(orderRequest))
                   .doFinally(mongoClient::close)
                   .subscribe(orderId -> promise.complete(
                       OrderResponse.builder().orderId(orderId).build()), error -> {
                     LOG.error("Some error occurred while saving order into database: {}",
                         error.getMessage());
                     promise.fail("Unable to save order into database");
                   });
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
      return promise.future();
    });
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
          JsonObject update = new JsonObject()
              .put(SET, new JsonObject().put(ORDER_STATS,
                  OrderStatus.valueOf(orderStatus)))
              .put(SET, new JsonObject().put(ORDER_UPDATED_AT,
                  LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a"))));
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
   * Calls the product service to validate the order items.
   *
   * @param orderItems the list of order items
   * @return a Future that will complete with the list of successfully validated order items
   */
  private Future<List<OrderItemRequest>> callToProductService(List<OrderItemRequest> orderItems) {

    Promise<List<OrderItemRequest>> promise = Promise.promise();
    LOG.info("*** Calling product-service to validate and place the order ***");

    WEB_CLIENT.put(PORT, HOST, PRODUCT_ORDER_ENDPOINT).rxSendJson(orderItems).subscribe(ar -> {
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
