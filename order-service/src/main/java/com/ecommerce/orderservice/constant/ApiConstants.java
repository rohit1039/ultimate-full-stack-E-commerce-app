package com.ecommerce.orderservice.constant;

public class ApiConstants {

  public static final String PRODUCT_ORDER_ENDPOINT = "/products/v1/order";
  public static final String PRODUCT_BY_ID_ENDPOINT = "/products/v1/get/";
  public static final String HOST = "localhost";
  public static final Integer PORT = 8081;
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String JSON_CONTENT_TYPE = "application/json";
  public static final String ORDER_ID = "_id";
  public static final String ORDER_STATS = "order_status";
  public static final String PRODUCT_ID = "product_id";
  public static final String PLACE_ORDER_ENDPOINT = "/v1/place-order";
  public static final String GET_ALL_ORDERS_ENDPOINT = "v1/orders"; // only by admins
  public static final String GET_ORDERS_BY_USER_ENDPOINT = "/v1/get-orders";
  public static final String ORDER_ITEMS = "order_items";

  public static final String ORDER_PLACED_BY = "order_placed_by";
  public static final String USERNAME = "username";
  public static final String COLLECTION = "orders";
  public static final Integer SUCCESS_STATUS_CODE = 200;
  public static final Integer ERROR_STATUS_CODE = 500;
  public static final Integer BAD_REQUEST_STATUS_CODE = 400;
  public static final Integer CREATED_STATUS_CODE = 201;
}
