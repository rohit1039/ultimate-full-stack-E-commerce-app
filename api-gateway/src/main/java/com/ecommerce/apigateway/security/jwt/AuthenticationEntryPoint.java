package com.ecommerce.apigateway.security.jwt;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

  @Override
  public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
    exchange.getResponse().getHeaders().add("Content-Type", "application/json");
    String errorResponse = "{\"error\": \"Access Denied\"}";
    return exchange
        .getResponse()
        .writeWith(
            Mono.just(exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes())));
  }
}
