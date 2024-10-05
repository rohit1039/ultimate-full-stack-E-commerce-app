package com.ecommerce.orderservice.verticle;

import com.ecommerce.orderservice.config.ConfigLoader;
import com.ecommerce.orderservice.payload.request.address.AddressRequest;
import com.ecommerce.orderservice.payload.request.order.OrderItemRequest;
import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import com.ecommerce.orderservice.payload.request.payment.PaymentRequest;
import com.ecommerce.orderservice.service.OrderService;
import com.ecommerce.orderservice.service.OrderServiceImpl;
import com.ecommerce.orderservice.validator.RequestValidator;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ecommerce.orderservice.constant.ApiConstants.ADDRESS;
import static com.ecommerce.orderservice.constant.ApiConstants.CREATE_ORDER_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.GET_ORDERS_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_ITEMS;
import static com.ecommerce.orderservice.constant.ApiConstants.PAYMENT;
import static com.ecommerce.orderservice.constant.ApiConstants.USERNAME;

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

    parentRoute.post(CREATE_ORDER_ENDPOINT).handler(this::placeOrder);
    parentRoute.get(GET_ORDERS_ENDPOINT).handler(this::getOrder);
  }

  public void placeOrder(RoutingContext routingContext) {

    AddressRequest addressRequest = routingContext.body()
                                                  .asJsonObject()
                                                  .getJsonObject(ADDRESS)
                                                  .mapTo(AddressRequest.class);
    String user = routingContext.request().getHeader(USERNAME) != null ? routingContext.request()
                                                                                       .getHeader(USERNAME) : null;
    String addressId = UUID.randomUUID().toString();
    addressRequest.setAddressId(addressId);
    addressRequest.setAddressCreationDate(LocalDateTime.now());
    addressRequest.setLastUpdatedAddressDate(LocalDateTime.now());
    addressRequest.setUsername(user);
    PaymentRequest paymentRequest = routingContext.body()
                                                  .asJsonObject()
                                                  .getJsonObject(PAYMENT)
                                                  .mapTo(PaymentRequest.class);
    UUID paymentId = UUID.randomUUID();
    paymentRequest.setTransactionId(String.valueOf(paymentId));
    paymentRequest.setPaymentDate(LocalDateTime.now());
    OrderRequest orderRequest = new OrderRequest();
    List<OrderItemRequest> orderItemRequest = routingContext.body()
                                                            .asJsonObject()
                                                            .getJsonArray(ORDER_ITEMS)
                                                            .stream()
                                                            .map(order -> new JsonObject(order.toString()).mapTo(OrderItemRequest.class))
                                                            .collect(Collectors.toList());
    orderRequest.setOrderItems(orderItemRequest);
    orderRequest.setShippingAddress(addressRequest);
    orderRequest.setUsername(user);
    orderRequest.setTransactionDetails(paymentRequest);
    boolean validationErrors = new RequestValidator().validateRequest(routingContext, orderRequest);
    if (!validationErrors) {
      createOrder(routingContext, orderRequest);
    }
  }

  private void createOrder(RoutingContext routingContext, OrderRequest orderRequest) {

    this.orderService.saveOrder(ConfigLoader.mongoConfig(), orderRequest, routingContext);
  }

  /**
   * @param routingContext to handle & customize request and response
   */
  public void getOrder(RoutingContext routingContext) {

    final String username = routingContext.request().getParam(USERNAME);
    this.orderService.retrieveOrders(ConfigLoader.mongoConfig(), username, routingContext);
  }
}
