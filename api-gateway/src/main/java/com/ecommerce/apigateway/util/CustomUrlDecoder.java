package com.ecommerce.apigateway.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class CustomUrlDecoder {

  public static String decodeValue(String path) {

    return URLDecoder.decode(path, StandardCharsets.UTF_8);
  }
}
