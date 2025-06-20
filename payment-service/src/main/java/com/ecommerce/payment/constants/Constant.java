package com.ecommerce.payment.constants;

public class Constant {

  public static final String API_GATEWAY_SERVER_URL = "http://localhost:8081/payments";

  public static final String PAYMENT_SERVICE_SERVER_URL = "http://localhost:8085";

  public static final String ORDER_SERVICE_SERVER_URL =
      "http://localhost:8081/orders/update-status";

  public static final String PRODUCT_SERVICE_PAYMENT_FAILURE_URL =
      "http://localhost:8081/products/v1/reserved-stocks/release";

  public static final String PRODUCT_SERVICE_PAYMENT_SUCCESS_URL =
      "http://localhost:8081/products/v1/confirm-stocks/count";
}
