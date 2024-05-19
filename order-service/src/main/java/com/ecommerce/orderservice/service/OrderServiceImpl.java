package com.ecommerce.orderservice.service;

import static com.ecommerce.orderservice.constant.APIConstants.PRODUCT_ENDPOINT;
import static com.ecommerce.orderservice.constant.APIConstants.PRODUCT_HOSTNAME;
import static com.ecommerce.orderservice.constant.APIConstants.PRODUCT_PORT;
import static com.ecommerce.orderservice.constant.APIConstants.PRODUCT_QUANTITY;
import static com.ecommerce.orderservice.constant.APIConstants.PRODUCT_SIZE;
import static com.ecommerce.orderservice.constant.APIConstants.SUCCESS_STATUS_CODE;

import com.ecommerce.orderservice.dao.OrderDao;
import com.ecommerce.orderservice.dao.OrderDaoImpl;
import com.ecommerce.orderservice.payload.request.order.OrderItemRequest;
import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import com.ecommerce.orderservice.payload.response.OrderResponse;
import com.ecommerce.orderservice.payload.response.OrderResponseBuilder;
import io.vertx.core.Future;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.client.WebClient;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements orderService interface and is used to place an order by calling the
 * product-service by specifying the product details.
 */
public class OrderServiceImpl implements OrderService {

  private static final Logger LOG = LoggerFactory.getLogger(OrderServiceImpl.class.getName());

  private final OrderDao orderDao = new OrderDaoImpl();

  private static final Vertx VERTX = Vertx.currentContext().owner();

  private final OrderResponseBuilder responseBuilder = new OrderResponseBuilder();

  private static final WebClient WEB_CLIENT = WebClient.create(VERTX);

  @Override
  public void saveOrder(
      final MongoClient mongoClient,
      final OrderRequest orderRequest,
      final RoutingContext routingContext) {

    List<OrderItemRequest> orderItems = orderRequest.getOrderItemList();

    orderItems.forEach(
        orderItem ->
            WEB_CLIENT
                .put(PRODUCT_PORT, PRODUCT_HOSTNAME, PRODUCT_ENDPOINT + orderItem.getProductId())
                .addQueryParam(PRODUCT_QUANTITY, String.valueOf(orderItem.getQuantity()))
                .addQueryParam(PRODUCT_SIZE, orderItem.getProductSize())
                .send()
                .subscribe(
                    productRes -> {
                      if (productRes.statusCode() == SUCCESS_STATUS_CODE) {
                        Future<OrderResponse> response =
                            this.orderDao.saveOrderIntoDB(mongoClient, orderRequest);
                        response.onSuccess(
                            res -> {
                              LOG.info("Order placed successfully with Id: {}", res.getOrderId());
                              responseBuilder.handleSuccessResponse(routingContext, res);
                            });
                        response.onFailure(
                            throwable -> {
                              LOG.error(
                                  "Some error occurred while placing the order: {}",
                                  throwable.getMessage());
                              responseBuilder.handleFailureResponse(routingContext, throwable);
                            });
                      }
                    },
                    error -> {
                      LOG.error(
                          "Error occurred while calling product-service: \n {}",
                          error.getMessage());
                      responseBuilder.handleFailureResponse(routingContext, error);
                    }));
  }
}
