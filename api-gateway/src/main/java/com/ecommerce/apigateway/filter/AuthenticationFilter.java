package com.ecommerce.apigateway.filter;

import com.ecommerce.apigateway.util.JwtUtil;
import com.ecommerce.apigateway.validator.RouteValidator;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * This class implements a Spring Cloud Gateway filter that authenticates requests based on JWT
 * tokens. The filter checks whether the request is secured (i.e., whether it requires
 * authentication) and if so, it extracts the JWT token from the request header and validates it. If
 * the validation is successful, the request is forwarded to the next filter in the chain.
 * Otherwise, the request is rejected with an "401 Unauthorized" error.
 */
@Component
public class AuthenticationFilter
    extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

  private final WebClient webClient;

  @Autowired private JwtUtil jwtUtil;

  @Autowired private RouteValidator validator;

  @Autowired
  public AuthenticationFilter(WebClient.Builder webClientBuilder) {

    super(Config.class);
    this.webClient = webClientBuilder.build();
  }

  @Override
  public GatewayFilter apply(Config config) {

    AtomicReference<String> token = new AtomicReference<>();
    return (exchange, chain) -> {
      if (validator.isSecured.test(exchange.getRequest())) {
        if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
          LOGGER.error("*** Missing authorization header ***");
          return this.onError(exchange, "Missing authorization header");
        }
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
          token.set(authHeader.substring(7));
        }
        return this.webClient
            .get()
            .uri("http://user-service/users/v1/current/role")
            .header("Authorization", authHeader)
            .retrieve()
            .bodyToMono(String.class)
            .flatMap(
                response -> {
                  exchange
                      .getRequest()
                      .mutate()
                      .header("loggedInUser", jwtUtil.extractUsername(token.get()))
                      .header("userRole", response)
                      .build();
                  return chain.filter(exchange);
                })
            .onErrorResume(
                throwable -> {
                  LOGGER.error(
                      "Some error occurred in ApiGateway: {}", throwable.getLocalizedMessage());
                  return this.onError(exchange, "Some error occurred");
                });
      }
      return chain.filter(exchange);
    };
  }

  private Mono<Void> onError(ServerWebExchange exchange, String err) {

    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    return response.setComplete();
  }

  public static class Config {}
}
