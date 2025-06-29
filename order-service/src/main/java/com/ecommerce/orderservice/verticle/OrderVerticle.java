package com.ecommerce.orderservice.verticle;

import static com.ecommerce.orderservice.constant.ApiConstants.CONTACT;
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

/**
 * The OrderVerticle class is responsible for handling routing and processing HTTP requests
 * associated with order-related operations in the application.
 * <p>
 * It extends the MainVerticle class and uses Vert.x framework components like Router and
 * RoutingContext
 * to define and manage REST endpoints for order functionalities.
 */
public class OrderVerticle extends MainVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(OrderVerticle.class.getName());

  private final Router router;
  private final OrderService orderService = new OrderServiceImpl();

  public OrderVerticle(Router router) {

    this.router = router;
  }

  /**
   * Starts the OrderVerticle by configuring the router with necessary routes.
   *
   * @param startFuture the promise that can be completed or failed to indicate the start status
   *                    of the verticle
   */
  @Override
  public void start(Promise<Void> startFuture) {

    configRoutes(router);
  }

  /**
   * Configures the routes for order-related operations in the application.
   *
   * @param parentRoute the {@code Router} instance to which the routes for order operations are
   *                    attached
   */
  public void configRoutes(Router parentRoute) {

    parentRoute.post(PLACE_ORDER_ENDPOINT).handler(this::placeOrder);
    parentRoute.get(GET_ORDERS_BY_USER_ENDPOINT).handler(this::getOrder);
    parentRoute.get(GET_ALL_ORDERS_ENDPOINT).handler(this::getOrders);
    parentRoute.patch(UPDATE_ORDER_STATS_ENDPOINT).handler(this::updateOrder);
    parentRoute.post(UPDATE_ORDER_PAYMENT_STATS_ENDPOINT).handler(this::updateOrderPaymentStats);
  }

  /**
   * Handles the process of placing an order by parsing the request data, processing the
   * order, and delegating the saving operation to the order service.
   *
   * @param routingContext the context of the HTTP request that contains request information,
   *                       including
   *                       headers and the request body for order creation.
   */
  public void placeOrder(RoutingContext routingContext) {

    LOG.info("Inside placeOrder");
    List<ApiErrorResponse> errorResponses = new ArrayList<>();
    JsonObject requestBody = routingContext.body().asJsonObject();
    String username = routingContext.request().getHeader(USERNAME);
    String contactNumber = routingContext.request().getHeader(CONTACT);
    this.orderService.saveOrder(ConfigLoader.mongoConfig(), requestBody, username, contactNumber,
        errorResponses,
        routingContext);
  }

  /**
   * Updates the status of an order based on the given order ID and status.
   * This method retrieves query parameters from the routing context to identify
   * the specific order and its updated status, then delegates the update operation to
   * the order service.
   *
   * @param routingContext the routing context that contains HTTP request data,
   *                       including query parameters such as order ID and order status
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
   * Updates the payment statistics of the specified order based on the request payload.
   * The method retrieves the order information from the request body, validates the inputs,
   * and invokes the order service to update the order's payment status and payment method.
   *
   * @param routingContext the routing context containing the HTTP request and response,
   *                       including the payload with required order details such as
   *                       orderId, paymentStatus, and paymentMethod.
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
   * Handles retrieving orders for a specific user based on the username provided in the request
   * header.
   *
   * @param routingContext the context of the current HTTP request, containing the request,
   *                       response, and other relevant data
   */
  public void getOrder(RoutingContext routingContext) {

    LOG.info("Inside getOrder");
    String username = routingContext.request().getHeader(USERNAME);
    this.orderService.retrieveOrders(ConfigLoader.mongoConfig(), username, routingContext);
  }

  /**
   * Handles the retrieval of all orders and processes the request.
   * Delegates to the {@code OrderService} to retrieve all order data from the database.
   *
   * @param routingContext the routing context for the HTTP request, containing the request,
   *                       response,
   *                       and other information required for processing the handler.
   */
  public void getOrders(RoutingContext routingContext) {

    LOG.info("Inside getOrders");
    this.orderService.retrieveAllOrders(ConfigLoader.mongoConfig(), routingContext);
  }

}
