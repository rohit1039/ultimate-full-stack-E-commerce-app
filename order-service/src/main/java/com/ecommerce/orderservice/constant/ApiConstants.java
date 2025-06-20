package com.ecommerce.orderservice.constant;

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
  public static final String ORDER_PLACED_BY = "order_placed_by";
  public static final String ORDER_PLACED_AT = "order_placed_at";
  public static final String ORDER_UPDATED_AT = "order_updated_at";
  public static final String PAYMENT_METHOD = "payment_method";
  public static final String USERNAME = "username";
  public static final String COLLECTION = "orders";
  public static final Integer SUCCESS_STATUS_CODE = 200;
  public static final Integer ERROR_STATUS_CODE = 500;
  public static final Integer BAD_REQUEST_STATUS_CODE = 400;
  public static final Integer CREATED_STATUS_CODE = 201;
}
