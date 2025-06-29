package com.ecommerce.orderservice.constant;

/**
 * The ApiConstants class provides a centralized set of constants used across the application.
 * These constants include API endpoint paths, header names, content types, status codes, and other
 * static attributes that serve as common references for request handling and processing.
 *
 * This class is specifically designed to provide constant strings and integers for API configurations,
 * ensuring consistency and avoiding duplication of literal values throughout the codebase.
 *
 * Fields:
 * - AUTH_HEADER: Represents the name of the authorization header.
 * - PRODUCT_ORDER_ENDPOINT: Endpoint for creating or handling product orders.
 * - PAYMENT_ORDER_ENDPOINT: Endpoint for initiating payment processing.
 * - PAYMENT_STATUS_ENDPOINT: Endpoint to check payment status.
 * - PRODUCT_BY_ID_ENDPOINT: Endpoint to fetch product details by ID.
 * - PRODUCT_HOST: Hostname for product-related services.
 * - PORT: Port number used for product service connection.
 * - PAYMENT_HOST: Hostname for payment-related services.
 * - CONTENT_TYPE: HTTP content type header name.
 * - JSON_CONTENT_TYPE: Value for application/json content type.
 * - ORDER_ID: Key representing order identifiers.
 * - ORDER_STATS: Field referring to the current status of an order.
 * - PRODUCT_ID: Field referring to product identifiers.
 * - PLACE_ORDER_ENDPOINT: API endpoint to place new orders.
 * - GET_ALL_ORDERS_ENDPOINT: API endpoint to fetch all orders, typically for admins.
 * - GET_ORDERS_BY_USER_ENDPOINT: API endpoint to fetch orders by a specific user.
 * - UPDATE_ORDER_STATS_ENDPOINT: API endpoint for updating order statuses, exclusively for admins.
 * - UPDATE_ORDER_PAYMENT_STATS_ENDPOINT: Endpoint for updating payment-related statuses of orders.
 * - PRODUCT_SERVICE_PAYMENT_FAILURE_URL: URL to handle payment failure scenarios for the product service.
 * - PRODUCT_SERVICE_PAYMENT_SUCCESS_URL: URL to handle payment success scenarios for the product service.
 * - STATUS: Generic field identifier for status.
 * - ID: Field key representing an identifier.
 * - SET: MongoDB operator used for setting values in documents.
 * - ORDER_ITEMS: Key to reference the collection of individual order items.
 * - ADDRESS: Key to represent the address associated with an order.
 * - ORDER_PLACED_BY: Field for the user account placing the order.
 * - ORDER_PLACED_AT: Timestamp indicating when the order was created.
 * - ORDER_UPDATED_AT: Timestamp indicating when the order was last updated.
 * - PAYMENT_METHOD: Key representing the order payment method.
 * - USERNAME: Field to represent usernames.
 * - CONTACT: Key to store or reference user contact details.
 * - COLLECTION: Represents the database collection name for storing orders.
 * - SUCCESS_STATUS_CODE: HTTP status code for successful requests.
 * - ERROR_STATUS_CODE: HTTP status code for server errors.
 * - BAD_REQUEST_STATUS_CODE: HTTP status code for invalid client requests.
 * - CREATED_STATUS_CODE: HTTP status code for successful resource creation.
 */
public class ApiConstants {

  public static final String AUTH_HEADER = "Authorization";
  public static final String PRODUCT_ORDER_ENDPOINT = "/products/v1/order";
  public static final String PAYMENT_ORDER_ENDPOINT = "/payments/checkout/";
  public static final String PAYMENT_STATUS_ENDPOINT = "/payments/status/";
  public static final String PRODUCT_BY_ID_ENDPOINT = "/products/v1/get/";
  public static final String PRODUCT_HOST = "localhost";
  public static final Integer PORT = 8081;
  public static final String PAYMENT_HOST = "localhost";
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String JSON_CONTENT_TYPE = "application/json";
  public static final String ORDER_ID = "_id";
  public static final String ORDER_STATS = "order_status";
  public static final String PRODUCT_ID = "product_id";
  public static final String PLACE_ORDER_ENDPOINT = "/orders/place-order";
  public static final String GET_ALL_ORDERS_ENDPOINT = "/orders/all"; // only by admins
  public static final String GET_ORDERS_BY_USER_ENDPOINT = "/orders/get-orders";
  public static final String UPDATE_ORDER_STATS_ENDPOINT = "/orders"; // only by admins
  public static final String UPDATE_ORDER_PAYMENT_STATS_ENDPOINT = "/orders/update-status"; // only by admins
  public static final String PRODUCT_SERVICE_PAYMENT_FAILURE_URL = "http://localhost:8081/products/v1/reserved-stocks/release";
  public static final String PRODUCT_SERVICE_PAYMENT_SUCCESS_URL = "http://localhost:8081/products/v1/confirm-stocks/count";
  public static final String STATUS = "status";
  public static final String ID = "id";
  public static final String SET = "$set";
  public static final String ORDER_ITEMS = "order_items";
  public static final String ADDRESS = "address";
  public static final String ORDER_PLACED_BY = "order_placed_by";
  public static final String ORDER_PLACED_AT = "order_placed_at";
  public static final String ORDER_UPDATED_AT = "order_updated_at";
  public static final String PAYMENT_METHOD = "payment_method";
  public static final String USERNAME = "username";
  public static final String CONTACT = "contact";
  public static final String COLLECTION = "orders";
  public static final Integer SUCCESS_STATUS_CODE = 200;
  public static final Integer ERROR_STATUS_CODE = 500;
  public static final Integer BAD_REQUEST_STATUS_CODE = 400;
  public static final Integer CREATED_STATUS_CODE = 201;
}
