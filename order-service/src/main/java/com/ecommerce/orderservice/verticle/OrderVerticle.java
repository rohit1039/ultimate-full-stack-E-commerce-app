package com.ecommerce.orderservice.verticle;

import static com.ecommerce.orderservice.constant.APIConstants.*;

import com.ecommerce.orderservice.config.ConfigLoader;
import com.ecommerce.orderservice.payload.request.order.OrderItemRequest;
import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import com.ecommerce.orderservice.service.OrderService;
import com.ecommerce.orderservice.service.OrderServiceImpl;
import com.ecommerce.orderservice.validator.RequestValidator;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderVerticle extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(OrderVerticle.class.getName());

  private final Router router;
  private final OrderService orderService = new OrderServiceImpl();

  public OrderVerticle(Router router) {
    this.router = router;
  }

  @Override
  public void start(Promise<Void> startFuture) {

    placeOrder(router);
  }

  public void placeOrder(Router parentRoute) {

    parentRoute.post(CREATE_ORDER_ENDPOINT).handler(this::handle);
  }

  public void handle(RoutingContext routingContext) {

    List<OrderItemRequest> orderItemList =
        routingContext.body().asJsonArray().stream()
            .map(order -> new JsonObject(order.toString()).mapTo(OrderItemRequest.class))
            .collect(Collectors.toList());
    OrderRequest orderRequest = new OrderRequest();
    orderRequest.setOrderItemList(orderItemList);
    boolean validationErrors = new RequestValidator().validateRequest(routingContext, orderRequest);
    if (!validationErrors) {
      createOrder(routingContext, orderRequest);
    }

    LOG.info("\n Incoming request: {}", OrderRequest.toJson(orderRequest).encodePrettily());
  }

  private void createOrder(RoutingContext routingContext, OrderRequest orderRequest) {

    this.orderService.saveOrder(ConfigLoader.mongoConfig(), orderRequest, routingContext);
  }
}
