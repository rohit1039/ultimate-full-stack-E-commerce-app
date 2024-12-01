package com.ecommerce.orderservice.verticle;

import static com.ecommerce.orderservice.constant.ApiConstants.GET_ALL_ORDERS_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.GET_ORDERS_BY_USER_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.ID;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_ITEMS;
import static com.ecommerce.orderservice.constant.ApiConstants.PLACE_ORDER_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.STATUS;
import static com.ecommerce.orderservice.constant.ApiConstants.UPDATE_ORDER_STATS_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.USERNAME;
import static com.ecommerce.orderservice.payload.request.order.OrderStatus.PENDING;

import com.ecommerce.orderservice.config.ConfigLoader;
import com.ecommerce.orderservice.payload.request.order.OrderItemRequest;
import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import com.ecommerce.orderservice.service.OrderService;
import com.ecommerce.orderservice.service.OrderServiceImpl;
import com.ecommerce.orderservice.validator.RequestValidator;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
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
  }

  public void placeOrder(RoutingContext routingContext) {

    LOG.info("Inside placeOrder");
    JsonObject requestBody = routingContext.body().asJsonObject();
    OrderRequest orderRequest = new OrderRequest();

    List<OrderItemRequest> orderItemRequest =
        requestBody.getJsonArray(ORDER_ITEMS)
                   .stream()
                   .map(order ->
                       new JsonObject(order.toString()).mapTo(OrderItemRequest.class))
                   .collect(Collectors.toList());

    orderRequest.setOrderItems(orderItemRequest);
    orderRequest.setOrderStatus(PENDING);
    orderRequest.setOrderPlacedAt(LocalDateTime.now());
    orderRequest.setOrderUpdatedAt(LocalDateTime.now());
    String username = routingContext.request().getHeader(USERNAME);
    boolean validationErrors = new RequestValidator().validateRequest(routingContext, orderRequest);
    if (!validationErrors) {
      LOG.info("No errors found, while saving the order");
      this.orderService.saveOrder(ConfigLoader.mongoConfig(), username, orderRequest, routingContext);
    }
  }

  /**
   * @param routingContext to handle & customize request and response
   */
  public void updateOrder(RoutingContext routingContext) {

    LOG.info("Inside updateOrder");
    MultiMap params = routingContext.queryParams();
    String orderId = params.get(ID);
    String orderStatus = params.get(STATUS);
    this.orderService.updateOrderById(ConfigLoader.mongoConfig(), orderId, orderStatus, routingContext);
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
