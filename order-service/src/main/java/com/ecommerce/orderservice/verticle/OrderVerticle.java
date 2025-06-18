package com.ecommerce.orderservice.verticle;

import static com.ecommerce.orderservice.constant.ApiConstants.GET_ALL_ORDERS_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.GET_ORDERS_BY_USER_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_ID;
import static com.ecommerce.orderservice.constant.ApiConstants.PLACE_ORDER_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.STATUS;
import static com.ecommerce.orderservice.constant.ApiConstants.UPDATE_ORDER_PAYMENT_STATS_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.UPDATE_ORDER_STATS_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.USERNAME;

import com.ecommerce.orderservice.config.ConfigLoader;
import com.ecommerce.orderservice.exception.ApiErrorResponse;
import com.ecommerce.orderservice.service.OrderService;
import com.ecommerce.orderservice.service.OrderServiceImpl;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderVerticle extends MainVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(OrderVerticle.class.getName());

  private final Router router;
  private final OrderService orderService = new OrderServiceImpl();

  public OrderVerticle(Router router) {

    this.router = router;
  }

  @Override
  public void start(Promise<Void> startFuture) {

    configRoutes(router);
  }

  public void configRoutes(Router parentRoute) {

    parentRoute.post(PLACE_ORDER_ENDPOINT).handler(this::placeOrder);
    parentRoute.get(GET_ORDERS_BY_USER_ENDPOINT).handler(this::getOrder);
    parentRoute.get(GET_ALL_ORDERS_ENDPOINT).handler(this::getOrders);
    parentRoute.patch(UPDATE_ORDER_STATS_ENDPOINT).handler(this::updateOrder);
    parentRoute.post(UPDATE_ORDER_PAYMENT_STATS_ENDPOINT).handler(this::updateOrderPaymentStats);
  }

  public void placeOrder(RoutingContext routingContext) {

    LOG.info("Inside placeOrder");
    List<ApiErrorResponse> errorResponses = new ArrayList<>();
    JsonObject requestBody = routingContext.body().asJsonObject();
    String username = routingContext.request().getHeader(USERNAME);
    this.orderService.saveOrder(ConfigLoader.mongoConfig(), requestBody, username, errorResponses,
        routingContext);
  }

  /**
   * @param routingContext to handle & customize request and response
   */
  public void updateOrder(RoutingContext routingContext) {

    LOG.info("Inside updateOrder");
    MultiMap params = routingContext.queryParams();
    String orderId = params.get(ORDER_ID);
    String orderStatus = params.get(STATUS);
    this.orderService.updateOrderById(ConfigLoader.mongoConfig(), orderId, orderStatus,
        routingContext);
  }

  /**
   *
   * @param routingContext to receive updated payment status
   */
  public void updateOrderPaymentStats(RoutingContext routingContext) {

    try {
      JsonObject body = routingContext.body().asJsonObject();

      String orderId = body.getString("orderId");
      String paymentStatus = body.getString("paymentStatus");
      String paymentMethod = body.getString("paymentMethod");

      if (orderId == null || paymentStatus == null) {
        routingContext.response()
           .setStatusCode(400).end("Missing orderId or paymentStatus");
      }
      this.orderService.updateOrderStats(ConfigLoader.mongoConfig(), orderId, paymentStatus, paymentMethod, routingContext);
    } catch (Exception e) {
      routingContext.response().setStatusCode(500).end("Invalid request payload");
    }
  }

  /**
   * @param routingContext to handle & customize request and response
   */
  public void getOrder(RoutingContext routingContext) {

    LOG.info("Inside getOrder");
    String username = routingContext.request().getHeader(USERNAME);
    this.orderService.retrieveOrders(ConfigLoader.mongoConfig(), username, routingContext);
  }

  /**
   * @param routingContext to handle & customize request and response
   */
  public void getOrders(RoutingContext routingContext) {

    LOG.info("Inside getOrders");
    this.orderService.retrieveAllOrders(ConfigLoader.mongoConfig(), routingContext);
  }

}
