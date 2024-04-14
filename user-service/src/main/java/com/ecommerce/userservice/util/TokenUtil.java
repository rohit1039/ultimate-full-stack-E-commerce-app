package com.ecommerce.userservice.util;

import net.minidev.json.JSONObject;

public class TokenUtil {

  public static JSONObject setToken(String token) {

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("token", token);
    return jsonObject;
  }
}
