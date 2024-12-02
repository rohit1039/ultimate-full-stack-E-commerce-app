package com.ecommerce.orderservice.service;

import static com.ecommerce.orderservice.constant.ApiConstants.BAD_REQUEST_STATUS_CODE;
import static com.ecommerce.orderservice.constant.ApiConstants.CREATED_STATUS_CODE;
import static com.ecommerce.orderservice.constant.ApiConstants.ERROR_STATUS_CODE;
import static com.ecommerce.orderservice.constant.ApiConstants.SUCCESS_STATUS_CODE;

import com.ecommerce.orderservice.dao.OrderDao;
import com.ecommerce.orderservice.dao.OrderDaoImpl;
import com.ecommerce.orderservice.exception.ApiErrorResponse;
import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import com.ecommerce.orderservice.payload.request.order.OrderStatus;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import com.ecommerce.orderservice.payload.response.OrderResponseBuilder;
import com.ecommerce.orderservice.payload.response.OrderResponseList;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.Future;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import io.vertx.rxjava3.ext.web.RoutingContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements orderService interface and is used to place an order by calling the
 * product-service by specifying the product details.
 */
public class OrderServiceImpl implements OrderService {

  private static final Logger LOG = LoggerFactory.getLogger(OrderServiceImpl.class.getName());

  private final OrderDao orderDao = new OrderDaoImpl();

  private final OrderResponseBuilder responseBuilder = new OrderResponseBuilder();

  @Override
  public void retrieveOrders(MongoClient mongoClient, String username,
                             RoutingContext routingContext) {

    Optional<String> header = Optional.ofNullable(username);
    header.ifPresentOrElse(user -> {
      Future<List<OrderResponseList>> orders = this.orderDao.getOrdersByUsername(mongoClient, user);
      orders.onSuccess(res -> {
        LOG.info("Orders found successfully for user: {}", user);
        try {
          responseBuilder.handleSuccessListResponse(routingContext, res);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      });
      orders.onFailure(throwable -> {
        LOG.error("Some error occurred while finding orders: \n {}", throwable.getMessage());
        responseBuilder.handleFailureResponse(routingContext, ERROR_STATUS_CODE, List.of(
            new ApiErrorResponse("Some error occurred while finding orders",
                throwable.getLocalizedMessage())));
      });
    }, () -> responseBuilder.handleFailureResponse(routingContext, BAD_REQUEST_STATUS_CODE, List.of(
        new ApiErrorResponse("No username provided in request header",
            "Please provide @RequestHeader 'username'"))));
  }

  @Override
  public void retrieveAllOrders(MongoClient mongoClient, RoutingContext routingContext) {

    Future<List<OrderResponseList>> orders = this.orderDao.getAllOrders(mongoClient);
    orders.onSuccess(res -> {
      LOG.info("Orders found successfully");
      try {
        responseBuilder.handleSuccessListResponse(routingContext, res);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    });
    orders.onFailure(throwable -> {
      LOG.error("Some error occurred while finding orders: \n {}", throwable.getMessage());
      responseBuilder.handleFailureResponse(routingContext, ERROR_STATUS_CODE, List.of(
          new ApiErrorResponse("Some error occurred while finding orders",
              throwable.getLocalizedMessage())));
    });
  }

  @Override
  public void saveOrder(final MongoClient mongoClient, final String username,
                        final OrderRequest orderRequest, final RoutingContext routingContext) {

    Optional<String> header = Optional.ofNullable(username);
    header.ifPresentOrElse(user -> {
      orderRequest.setOrderPlacedBy(user);
      Future<OrderResponse> orderInDb = this.orderDao.saveOrder(mongoClient, orderRequest);
      orderInDb.onSuccess(res -> {
        LOG.info("Order placed successfully with Id: {}", res.getOrderId());
        responseBuilder.handleSuccessResponse(routingContext, CREATED_STATUS_CODE, res);
      });
      orderInDb.onFailure(throwable -> {
        LOG.error("Some error occurred while placing the order: \n {}", throwable.getMessage());
        responseBuilder.handleFailureResponse(routingContext, ERROR_STATUS_CODE, List.of(
            new ApiErrorResponse("Some error occurred while placing the order",
                throwable.getMessage())));
      });
    }, () -> responseBuilder.handleFailureResponse(routingContext, BAD_REQUEST_STATUS_CODE, List.of(
        new ApiErrorResponse("No username provided in request header",
            "Please provide @RequestHeader 'username'"))));
  }

  @Override
  public void updateOrderById(MongoClient mongoClient, String orderId, String orderStatus,
                              RoutingContext routingContext) {

    List<ApiErrorResponse> errorResponses = new ArrayList<>();

    if (Objects.isNull(orderId) || Objects.isNull(orderStatus)) {
      LOG.error("Please provide request params i.e. (id and status) to update the order status");
      errorResponses.add(new ApiErrorResponse("Some error occurred while updating the order",
          "Please provide request params i.e. (id and status) to update the order status"));
    }
    if (!OrderStatus.isValid(orderStatus)) {
      LOG.error("Please provide a valid order status value");
      errorResponses.add(new ApiErrorResponse("Some error occurred while updating the order",
          "Please provide a valid order status value"));
    }

    // Handle validation errors
    if (!errorResponses.isEmpty()) {
      responseBuilder.handleFailureResponse(routingContext, BAD_REQUEST_STATUS_CODE,
          errorResponses);
      return;
    }
    Future<OrderResponse> orderInDb = this.orderDao.updateOrder(mongoClient, orderId, orderStatus);

    orderInDb.onSuccess(res -> {
      LOG.info("Order updated successfully with Id: {}", res.getOrderId());
      responseBuilder.handleSuccessResponse(routingContext, SUCCESS_STATUS_CODE, res);
    });

    orderInDb.onFailure(throwable -> {
      LOG.error("Some error occurred while updating the order: \n {}", throwable.getMessage());
      responseBuilder.handleFailureResponse(routingContext, ERROR_STATUS_CODE, List.of(
          new ApiErrorResponse("Some error occurred while updating the order",
              throwable.getMessage())));
    });
  }

}
