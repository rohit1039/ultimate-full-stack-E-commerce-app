package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dao.OrderDao;
import com.ecommerce.orderservice.dao.OrderDaoImpl;
import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import com.ecommerce.orderservice.payload.response.OrderResponseBuilder;
import io.vertx.core.Future;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import io.vertx.rxjava3.ext.web.RoutingContext;
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
  public void saveOrder(
      final MongoClient mongoClient,
      final OrderRequest orderRequest,
      final RoutingContext routingContext) {

    Future<OrderResponse> orderInDb =
        this.orderDao.saveOrderInDb(mongoClient, routingContext, orderRequest);

    orderInDb.onSuccess(
        res -> {
          LOG.info("Order placed successfully with Id: {}", res.getOrderId());
          responseBuilder.handleSuccessResponse(routingContext, res);
        });
    orderInDb.onFailure(
        throwable -> {
          LOG.error("Some error occurred while placing the order: \n {}", throwable.getMessage());
          responseBuilder.handleFailureResponse(routingContext, throwable);
        });
  }
}
