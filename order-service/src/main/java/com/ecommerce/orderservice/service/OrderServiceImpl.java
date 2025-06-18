package com.ecommerce.orderservice.service;

import static com.ecommerce.orderservice.constant.ApiConstants.AUTH_HEADER;
import static com.ecommerce.orderservice.constant.ApiConstants.BAD_REQUEST_STATUS_CODE;
import static com.ecommerce.orderservice.constant.ApiConstants.CREATED_STATUS_CODE;
import static com.ecommerce.orderservice.constant.ApiConstants.ERROR_STATUS_CODE;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_ITEMS;
import static com.ecommerce.orderservice.constant.ApiConstants.SUCCESS_STATUS_CODE;
import static com.ecommerce.orderservice.payload.request.order.OrderStatus.PENDING;

import com.ecommerce.orderservice.dao.OrderDao;
import com.ecommerce.orderservice.dao.OrderDaoImpl;
import com.ecommerce.orderservice.exception.ApiErrorResponse;
import com.ecommerce.orderservice.payload.request.order.OrderItemRequest;
import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import com.ecommerce.orderservice.payload.request.order.OrderStatus;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import com.ecommerce.orderservice.payload.response.OrderResponseBuilder;
import com.ecommerce.orderservice.payload.response.OrderResponseList;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import io.vertx.rxjava3.ext.web.RoutingContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderServiceImpl implements OrderService {

  private static final Logger LOG = LoggerFactory.getLogger(OrderServiceImpl.class);

  private final OrderDao orderDao = new OrderDaoImpl();
  private final OrderResponseBuilder responseBuilder = new OrderResponseBuilder();

  @Override
  public void retrieveOrders(MongoClient mongoClient, String username,
                             RoutingContext routingContext) {

    Optional.ofNullable(username)
            .ifPresentOrElse(user -> handleRetrieveOrders(mongoClient, user, routingContext),
                () -> {
                  responseBuilder.handleFailureResponse(routingContext, BAD_REQUEST_STATUS_CODE,
                      List.of(new ApiErrorResponse("No username provided in request header",
                          "Please authenticate through JWT")));
                });
  }

  @Override
  public void updateOrderStats(MongoClient mongoClient, String orderId, String paymentStatus,
                               String paymentMethod, RoutingContext routingContext) {

    Future<OrderResponse> orders =
        orderDao.updateOrderStats(mongoClient, orderId, paymentStatus, paymentMethod);
    orders.onSuccess(res -> {
            LOG.info("Order status updated successfully with id: {}", orderId);
            responseBuilder.handleSuccessResponse(routingContext, SUCCESS_STATUS_CODE, res);
          })
          .onFailure(throwable -> handleFailureResponse(routingContext, throwable,
              "Some error occurred while updating order status with id: " + orderId));
  }

  private void handleRetrieveOrders(MongoClient mongoClient, String username,
                                    RoutingContext routingContext) {

    Future<List<OrderResponseList>> orders = orderDao.getOrdersByUsername(mongoClient, username);
    orders.onSuccess(res -> {
      LOG.info("Orders found successfully for user: {}", username);
      try {
        responseBuilder.handleSuccessListResponse(routingContext, res);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }).onFailure(throwable -> handleFailureResponse(routingContext, throwable,
              "Some error occurred while finding orders"));
  }

  @Override
  public void retrieveAllOrders(MongoClient mongoClient, RoutingContext routingContext) {

    Future<List<OrderResponseList>> orders = orderDao.getAllOrders(mongoClient);
    orders.onSuccess(res -> {
      LOG.info("Orders found successfully");
      try {
        responseBuilder.handleSuccessListResponse(routingContext, res);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }).onFailure(throwable -> handleFailureResponse(routingContext, throwable,
        "Some error occurred while finding orders"));
  }

  @Override
  public void saveOrder(MongoClient mongoClient, JsonObject requestBody, String username,
                        List<ApiErrorResponse> errorResponses, RoutingContext routingContext) {

    if (validateSaveOrderRequest(requestBody, errorResponses)) {
      responseBuilder.handleFailureResponse(routingContext, BAD_REQUEST_STATUS_CODE,
          errorResponses);
      return;
    }

    List<OrderItemRequest> orderItems = extractOrderItems(requestBody, errorResponses);
    if (validateOrderItems(orderItems, errorResponses)) {
      responseBuilder.handleFailureResponse(routingContext, BAD_REQUEST_STATUS_CODE,
          errorResponses);
      return;
    }

    createAndSaveOrder(mongoClient, username, orderItems, routingContext);
  }

  private boolean validateSaveOrderRequest(JsonObject requestBody,
                                           List<ApiErrorResponse> errorResponses) {

    if (Objects.isNull(requestBody) || requestBody.isEmpty()) {
      errorResponses.add(
          new ApiErrorResponse("Unable to place order", "order_items array is required"));
      return true;
    }
    JsonArray orderItems = requestBody.getJsonArray(ORDER_ITEMS);
    if (orderItems.isEmpty()) {
      errorResponses.add(
          new ApiErrorResponse("Unable to place order", "order_items array should not be empty"));
      return true;
    }
    return false;
  }

  private List<OrderItemRequest> extractOrderItems(JsonObject requestBody,
                                                   List<ApiErrorResponse> errorResponses) {

    JsonArray orderItems = requestBody.getJsonArray(ORDER_ITEMS);
    return orderItems.stream()
                     .map(order -> new JsonObject(order.toString()).mapTo(OrderItemRequest.class))
                     .collect(Collectors.toList());
  }

  private boolean validateOrderItems(List<OrderItemRequest> orderItems,
                                     List<ApiErrorResponse> errorResponses) {

    orderItems.forEach(orderItem -> {
      if (orderItem.getProductId() <= 0) {
        errorResponses.add(
            new ApiErrorResponse("Unable to place order for id: " + orderItem.getProductId(),
                "product_id cannot be less than or equal to 0"));
      }
      if (Objects.isNull(orderItem.getProductSize()) || orderItem.getProductSize().isEmpty()) {
        errorResponses.add(
            new ApiErrorResponse("Unable to place order for id: " + orderItem.getProductId(),
                "product_size cannot be empty or null"));
      }
      if (orderItem.getQuantity() <= 0) {
        errorResponses.add(
            new ApiErrorResponse("Unable to place order for id: " + orderItem.getProductId(),
                "quantity cannot be less than or equal to 0"));
      }
    });
    return !errorResponses.isEmpty();
  }

  private void createAndSaveOrder(MongoClient mongoClient, String username,
                                  List<OrderItemRequest> orderItems,
                                  RoutingContext routingContext) {

    OrderRequest orderRequest = new OrderRequest();
    orderRequest.setOrderItems(orderItems);
    orderRequest.setOrderStatus(PENDING);
    orderRequest.setOrderPlacedAt(LocalDateTime.now());
    orderRequest.setOrderUpdatedAt(LocalDateTime.now());
    orderRequest.setOrderPlacedBy(username);

    String token = routingContext.request().getHeader(AUTH_HEADER);

    Optional.ofNullable(username).ifPresentOrElse(user -> {
      Future<OrderResponse> orderInDb = orderDao.saveOrder(mongoClient, orderRequest, token);
      orderInDb.onSuccess(res -> {
                 LOG.info("Order placed successfully with Id: {}", res.getOrderId());
                 responseBuilder.handleSuccessResponse(routingContext, CREATED_STATUS_CODE, res);
               })
               .onFailure(throwable -> handleFailureResponse(routingContext, throwable,
                   "Some error occurred while placing the order"));
    }, () -> responseBuilder.handleFailureResponse(routingContext, BAD_REQUEST_STATUS_CODE, List.of(
        new ApiErrorResponse("No username provided in request header",
            "Please authenticate through JWT"))));
  }

  @Override
  public void updateOrderById(MongoClient mongoClient, String orderId, String orderStatus,
                              RoutingContext routingContext) {

    List<ApiErrorResponse> errorResponses = validateUpdateOrderRequest(orderId, orderStatus);
    if (!errorResponses.isEmpty()) {
      responseBuilder.handleFailureResponse(routingContext, BAD_REQUEST_STATUS_CODE,
          errorResponses);
      return;
    }

    Future<OrderResponse> orderInDb = orderDao.updateOrder(mongoClient, orderId, orderStatus);
    orderInDb.onSuccess(res -> {
               LOG.info("Order updated successfully with Id: {}", res.getOrderId());
               responseBuilder.handleSuccessResponse(routingContext, SUCCESS_STATUS_CODE, res);
             })
             .onFailure(throwable -> handleFailureResponse(routingContext, throwable,
                 "Some error occurred while updating the order"));
  }

  private List<ApiErrorResponse> validateUpdateOrderRequest(String orderId, String orderStatus) {

    List<ApiErrorResponse> errorResponses = new ArrayList<>();
    if (Objects.isNull(orderId) || Objects.isNull(orderStatus)) {
      LOG.error("Please provide request params i.e. (id and status) to update the order status");
      errorResponses.add(new ApiErrorResponse("Some error occurred while updating the order",
          "Order ID and status are required"));
    }
    if (!OrderStatus.isValid(orderStatus)) {
      LOG.error("Please provide a valid order status value");
      errorResponses.add(new ApiErrorResponse("Some error occurred while updating the order",
          "Valid order status is required"));
    }
    return errorResponses;
  }

  private void handleFailureResponse(RoutingContext routingContext, Throwable throwable,
                                     String errorMessage) {

    LOG.error(errorMessage + ": \n {}", throwable.getMessage());
    responseBuilder.handleFailureResponse(routingContext, ERROR_STATUS_CODE,
        List.of(new ApiErrorResponse(errorMessage, throwable.getLocalizedMessage())));
  }

}

