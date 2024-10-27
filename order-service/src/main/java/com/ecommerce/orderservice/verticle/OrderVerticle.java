package com.ecommerce.orderservice.verticle;

import static com.ecommerce.orderservice.constant.ApiConstants.ADDRESS;
import static com.ecommerce.orderservice.constant.ApiConstants.GET_ORDERS_BY_USER_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_ITEMS;
import static com.ecommerce.orderservice.constant.ApiConstants.PAYMENT;
import static com.ecommerce.orderservice.constant.ApiConstants.PLACE_ORDER_ENDPOINT;
import static com.ecommerce.orderservice.constant.ApiConstants.USERNAME;

import com.ecommerce.orderservice.config.ConfigLoader;
import com.ecommerce.orderservice.payload.request.address.AddressRequest;
import com.ecommerce.orderservice.payload.request.order.OrderItemRequest;
import com.ecommerce.orderservice.payload.request.order.OrderRequest;
import com.ecommerce.orderservice.payload.request.order.OrderStatus;
import com.ecommerce.orderservice.payload.request.payment.PaymentRequest;
import com.ecommerce.orderservice.payload.request.payment.PaymentStatus;
import com.ecommerce.orderservice.service.OrderService;
import com.ecommerce.orderservice.service.OrderServiceImpl;
import com.ecommerce.orderservice.validator.RequestValidator;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
  }

  public void placeOrder(RoutingContext routingContext) {

    LOG.info("Inside placeOrder");
    JsonObject requestBody = routingContext.body().asJsonObject();
    AddressRequest addressRequest = requestBody.getJsonObject(ADDRESS).mapTo(AddressRequest.class);
    String addressId = UUID.randomUUID().toString();
    addressRequest.setAddressId(addressId);
    addressRequest.setAddressCreationDate(LocalDateTime.now());
    addressRequest.setLastUpdatedAddressDate(LocalDateTime.now());
    Optional<String> user = Optional.ofNullable(routingContext.request().getHeader(USERNAME));
    user.ifPresent(addressRequest::setUsername);
    LOG.info("Address set complete");
    PaymentRequest paymentRequest = requestBody.getJsonObject(PAYMENT).mapTo(PaymentRequest.class);
    UUID paymentId = UUID.randomUUID();
    paymentRequest.setStatus(PaymentStatus.PENDING);
    paymentRequest.setTransactionId(String.valueOf(paymentId));
    paymentRequest.setPaymentDate(LocalDateTime.now());
    LOG.info("Payment set complete");
    OrderRequest orderRequest = new OrderRequest();
    List<OrderItemRequest> orderItemRequest = requestBody.getJsonArray(ORDER_ITEMS)
                                                         .stream()
                                                         .map(order -> new JsonObject(
                                                             order.toString()).mapTo(
                                                             OrderItemRequest.class))
                                                         .collect(Collectors.toList());
    orderRequest.setOrderItems(orderItemRequest);
    orderRequest.setShippingAddress(addressRequest);
    orderRequest.setOrderStatus(OrderStatus.PENDING);
    orderRequest.setOrderPlaceAt(LocalDateTime.now());
    orderRequest.setOrderUpdatedAt(LocalDateTime.now());
    user.ifPresent(orderRequest::setOrderPlacedBy);
    orderRequest.setTransactionDetails(paymentRequest);
    boolean validationErrors = new RequestValidator().validateRequest(routingContext, orderRequest);
    if (!validationErrors) {
      LOG.info("No errors found, while saving the order");
      this.orderService.saveOrder(ConfigLoader.mongoConfig(), orderRequest, routingContext);
    }
  }

  /**
   * @param routingContext to handle & customize request and response
   */
  public void getOrder(RoutingContext routingContext) {

    LOG.info("Inside getOrder");
    Optional<String> username = Optional.ofNullable(routingContext.request().getHeader(USERNAME));
    username.ifPresent(
        user -> this.orderService.retrieveOrders(ConfigLoader.mongoConfig(), user, routingContext));
  }
}
