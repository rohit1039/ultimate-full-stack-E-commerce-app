package com.ecommerce.orderservice.service;

import static com.ecommerce.orderservice.constant.ApiConstants.ADDRESS;
import static com.ecommerce.orderservice.constant.ApiConstants.AUTH_HEADER;
import static com.ecommerce.orderservice.constant.ApiConstants.BAD_REQUEST_STATUS_CODE;
import static com.ecommerce.orderservice.constant.ApiConstants.CREATED_STATUS_CODE;
import static com.ecommerce.orderservice.constant.ApiConstants.ERROR_STATUS_CODE;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_ITEMS;
import static com.ecommerce.orderservice.constant.ApiConstants.SUCCESS_STATUS_CODE;
import static com.ecommerce.orderservice.payload.request.order.OrderStatus.PENDING;

import com.ecommerce.orderservice.config.AddressSeedLoader;
import com.ecommerce.orderservice.dao.OrderDao;
import com.ecommerce.orderservice.dao.OrderDaoImpl;
import com.ecommerce.orderservice.exception.ApiErrorResponse;
import com.ecommerce.orderservice.exception.ClientInputException;
import com.ecommerce.orderservice.payload.request.address.AddressRequest;
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

/**
 * Service implementation for managing orders.
 * This class provides various operations for order management, including retrieving,
 * updating, and saving orders in the database.
 * <p>
 * Implements the {@link OrderService} interface.
 */
public class OrderServiceImpl implements OrderService {

  private static final Logger LOG = LoggerFactory.getLogger(OrderServiceImpl.class);

  private final OrderDao orderDao = new OrderDaoImpl();
  private final OrderResponseBuilder responseBuilder = new OrderResponseBuilder();

  /**
   * Retrieves orders for a specific user.
   * <p>
   * This method checks if the provided username is present. If the username is valid, it delegates
   * the retrieval process to the handleRetrieveOrders method, which fetches the orders from the
   * data layer and sends the appropriate response. If the username is not present, it sends a
   * failure response with a bad request status code.
   *
   * @param mongoClient    the MongoClient instance used to interact with the database
   * @param username       the username of the user whose orders are to be retrieved
   * @param routingContext the routing context that contains information related to the HTTP
   *                       request and response
   */
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

  /**
   * Updates the order statistics including payment status and payment method for a given order.
   * Handles success and failure scenarios while interacting with the underlying data layer.
   *
   * @param mongoClient     the MongoDB client used to interact with the database
   * @param orderId         the unique identifier of the order to update
   * @param paymentStatus   the new payment status to update for the order
   * @param paymentMethod   the payment method used for the order
   * @param routingContext  the routing context used to manage the HTTP request and response
   */
  @Override
  public void updateOrderStats(MongoClient mongoClient, String orderId, String paymentStatus,
                               String paymentMethod, RoutingContext routingContext) {

    Future<OrderResponse> orders = orderDao.updateOrderStats(mongoClient, orderId, paymentStatus, paymentMethod);
    orders.onSuccess(res -> {
      LOG.info("Order status updated successfully with id: {}", orderId);
      responseBuilder.handleSuccessResponse(routingContext, SUCCESS_STATUS_CODE, res);
    }).onFailure(throwable -> handleFailureResponse(routingContext, throwable,
        "Some error occurred while updating order status with id: " + orderId));
  }

  /**
   * Handles the retrieval of orders for a specific user.
   *
   * This method interacts with the data access layer to fetch orders associated with a given
   * username
   * and sends the appropriate response back to the client. In case of success, it formats and
   * returns
   * the list of orders. If an error occurs, it logs the issue and sends a failure response to
   * the client.
   *
   * @param mongoClient    the MongoClient instance used to interact with the database
   * @param username       the username of the user whose orders are to be retrieved
   * @param routingContext the routing context that contains information related to the HTTP request
   */
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

  /**
   * Retrieves all orders from the database and sends the response to the client.
   *
   * @param mongoClient the Mongo client instance used to communicate with the database
   * @param routingContext the routing context instance used to handle HTTP request and response
   */
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

  /**
   * Saves an order after validating the request and order items, and persists the order in the
   * database.
   *
   * @param mongoClient The instance of MongoClient used for database interaction.
   * @param requestBody The request body containing order details including address and items.
   * @param username The username of the user placing the order.
   * @param contactNumber The contact number of the user placing the order.
   * @param errorResponses A list used to collect any validation errors encountered during the
   *                       process.
   * @param routingContext The routing context for sending responses.
   */
  @Override
  public void saveOrder(MongoClient mongoClient, JsonObject requestBody, String username, String contactNumber,
                        List<ApiErrorResponse> errorResponses, RoutingContext routingContext) {

    new AddressSeedLoader().run();

    JsonObject address = requestBody.getJsonObject(ADDRESS);

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

    createAndSaveOrder(mongoClient, address, username, contactNumber, orderItems, routingContext);
  }

  /**
   * Validates the request body for saving an order and populates error responses if validation
   * fails.
   *
   * @param requestBody a JsonObject containing the order details to be validated
   * @param errorResponses a list to which validation errors will be added if the request is invalid
   * @return true if the request is invalid and contains errors, false otherwise
   */
  private boolean validateSaveOrderRequest(JsonObject requestBody,
                                           List<ApiErrorResponse> errorResponses) {

    if (Objects.isNull(requestBody) || requestBody.isEmpty()) {
      errorResponses.add(
          new ApiErrorResponse("Unable to place order", "order_items array is required"));
      return true;
    }
    JsonArray orderItems = requestBody.getJsonArray(ORDER_ITEMS);
    JsonObject address = requestBody.getJsonObject(ADDRESS);
    AddressRequest addressRequest = address.mapTo(AddressRequest.class);

    if (orderItems.isEmpty()) {
      errorResponses.add(
          new ApiErrorResponse("Unable to place order", "order_items array should not be empty"));
      return true;
    }

    return validateAddress(addressRequest, errorResponses);
  }

  /**
   * Extracts order items from the provided request body and maps them to a list of
   * {@link OrderItemRequest}.
   *
   * @param requestBody the JSON object containing the order details, including the order item
   *                    information
   * @param errorResponses a list to collect any potential errors that occur during the
   *                       extraction or mapping process
   * @return a list of {@link OrderItemRequest} objects representing the order items in the request
   */
  private List<OrderItemRequest> extractOrderItems(JsonObject requestBody,
                                                   List<ApiErrorResponse> errorResponses) {

    JsonArray orderItems = requestBody.getJsonArray(ORDER_ITEMS);
    return orderItems.stream()
                     .map(order -> new JsonObject(order.toString()).mapTo(OrderItemRequest.class))
                     .collect(Collectors.toList());
  }

  /**
   * Validates the provided address object by performing several checks on its fields
   * (e.g., postal code format, mandatory fields like city, district, and state).
   * Any validation errors encountered are added to the provided list of error responses.
   *
   * @param address the address object to be validated
   * @param errorResponses a list to which validation error messages are added
   * @return {@code true} if any validation errors are found in the address, {@code false} otherwise
   */
  private boolean validateAddress(AddressRequest address, List<ApiErrorResponse> errorResponses) {

    if (address == null) {
      errorResponses.add(new ApiErrorResponse("Invalid address", "Address object is required"));
      return true;
    }
    Long pincode = address.getPostalCode();
    String city = address.getCityName();
    String district = address.getDistrict();

    if (pincode == null) {
      errorResponses.add(new ApiErrorResponse("Invalid address", "pinCode is required"));
      return true;
    }
    if (city == null || city.isBlank()) {
      errorResponses.add(new ApiErrorResponse("Invalid address", "city is required"));
      return true;
    }
    if (district == null || district.isBlank()) {
      errorResponses.add(new ApiErrorResponse("Invalid address", "district is required"));
      return true;
    }
    String state = address.getStateName();
    if (state == null || state.isBlank()) {
      errorResponses.add(new ApiErrorResponse("Invalid address", "state is required"));
      return true;
    }
    if (!pincode.toString().matches("\\d{6}")) {
      errorResponses.add(new ApiErrorResponse("Invalid address", "pinCode must be a 6-digit number"));
      return true;
    }
    return false;
  }

  /**
   * Validates the list of order items and populates the list of error responses with any
   * validation errors.
   * Each order item is checked for a valid product ID, a non-empty and non-null size, and a
   * quantity greater than zero.
   *
   * @param orderItems the list of {@code OrderItemRequest} objects to validate
   * @param errorResponses the list of {@code ApiErrorResponse} objects to be populated with
   *                       validation errors
   * @return {@code true} if any validation errors are encountered; {@code false} otherwise
   */
  private boolean validateOrderItems(List<OrderItemRequest> orderItems,
                                     List<ApiErrorResponse> errorResponses) {

    orderItems.forEach(orderItem -> {
      if (orderItem.getProductId() <= 0) {
        errorResponses.add(
            new ApiErrorResponse("Unable to place order for id: " + orderItem.getProductId(),
                "product_id cannot be less than or equal to 0"));
      }
      if (Objects.isNull(orderItem.getSize()) || orderItem.getSize().isEmpty()) {
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

  /**
   * Creates and saves a new order in the system. This method constructs an
   * {@code OrderRequest} object with the provided details, attempts to save
   * it in the database, and sends appropriate responses based on success or
   * failure.
   *
   * @param mongoClient the MongoDB client for database operations
   * @param address a {@code JsonObject} representing the address details of the order
   * @param username the username of the individual placing the order
   * @param contactNumber the contact number of the individual placing the order
   * @param orderItems a list of {@code OrderItemRequest} objects representing items in the order
   * @param routingContext the routing context used to handle HTTP responses
   */
  private void createAndSaveOrder(MongoClient mongoClient, JsonObject address, String username, String contactNumber,
                                  List<OrderItemRequest> orderItems,
                                  RoutingContext routingContext) {

    OrderRequest orderRequest = new OrderRequest();
    orderRequest.setOrderItems(orderItems);
    orderRequest.setOrderStatus(PENDING);
    orderRequest.setOrderPlacedAt(LocalDateTime.now());
    orderRequest.setOrderUpdatedAt(LocalDateTime.now());
    orderRequest.setOrderPlacedBy(username);
    orderRequest.setAddress(address.mapTo(AddressRequest.class));

    String token = routingContext.request().getHeader(AUTH_HEADER);
    Optional.ofNullable(username).ifPresentOrElse(user -> {
      Future<OrderResponse> orderInDb = orderDao.saveOrder(mongoClient, orderRequest, username, contactNumber, token);
      orderInDb.onSuccess(res -> {
        LOG.info("Order placed successfully with Id: {}", res.getOrderId());
        responseBuilder.handleSuccessResponse(routingContext, CREATED_STATUS_CODE, res);
      }).onFailure(throwable -> {
        if (throwable instanceof ClientInputException) {
          responseBuilder.handleFailureResponse(routingContext, BAD_REQUEST_STATUS_CODE,
              List.of(new ApiErrorResponse("Validation Failed", throwable.getMessage())));
        } else {
          handleFailureResponse(routingContext, throwable, "Some error occurred while placing the order");
        }
      });
    }, () -> responseBuilder.handleFailureResponse(routingContext, BAD_REQUEST_STATUS_CODE, List.of(
        new ApiErrorResponse("No username provided in request header",
            "Please authenticate through JWT"))));
  }

  /**
   * Updates the status of an order by its ID in the database.
   * Validates the input parameters, performs the update operation,
   * and constructs the response accordingly.
   *
   * @param mongoClient the MongoDB client used for database operations
   * @param orderId the unique identifier of the order to update
   * @param orderStatus the new status to apply to the order
   * @param routingContext the routing context containing request and response details
   */
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
    }).onFailure(throwable -> handleFailureResponse(routingContext, throwable,
                 "Some error occurred while updating the order"));
  }

  /**
   * Validates the input parameters for updating an order and returns a list of validation
   * errors, if any.
   * It checks whether the order ID and status are provided and whether the order status is valid.
   *
   * @param orderId the unique identifier of the order to be updated
   * @param orderStatus the new status of the order to be updated
   * @return a list of {@link ApiErrorResponse} containing error messages if validation fails; an
   * empty list if validation passes
   */
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

  /**
   * Handles the failure response by logging the error and sending an appropriately formatted
   * error response back to the client.
   *
   * @param routingContext the context for handling the HTTP request and response
   * @param throwable the exception or error causing the failure
   * @param errorMessage the error message to be returned and logged
   */
  private void handleFailureResponse(RoutingContext routingContext, Throwable throwable,
                                     String errorMessage) {

    LOG.error(errorMessage + ": \n {}", throwable.getMessage());
    responseBuilder.handleFailureResponse(routingContext, ERROR_STATUS_CODE,
        List.of(new ApiErrorResponse(errorMessage, throwable.getLocalizedMessage())));
  }

}

