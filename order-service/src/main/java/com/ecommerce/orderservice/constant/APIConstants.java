package com.ecommerce.orderservice.constant;

public class APIConstants {

  public static final String PRODUCT_ENDPOINT = "/products/v1/order-product/";
  public static final String PRODUCT_QUANTITY = "quantity";
  public static final String PRODUCT_HOSTNAME = "localhost";
  public static final Integer PRODUCT_PORT = 8083;
  public static final String PRODUCT_SIZE = "productSize";
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String JSON_CONTENT_TYPE = "application/json";
  public static final String ORDER_ID = "order_id";
  public static final String CREATE_ORDER_ENDPOINT = "/v1/place-order";
  public static final String COLLECTION_NAME = "orders";
  public static final Integer SUCCESS_STATUS_CODE = 200;
  public static final Integer ERROR_STATUS_CODE = 500;
  public static final Integer CREATED_STATUS_CODE = 201;
}
