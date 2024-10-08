package com.ecommerce.apigateway.util;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class SecurityUtils {

  private static final String ROLE_PREFIX = "ROLE_";

  public static SimpleGrantedAuthority convertToAuthority(String role) {

    String formattedRole = role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role;
    return new SimpleGrantedAuthority(formattedRole);
  }
}
