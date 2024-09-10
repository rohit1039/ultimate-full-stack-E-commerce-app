package com.ecommerce.apigateway.filter;

import com.ecommerce.apigateway.security.CustomUserDetails;
import com.ecommerce.apigateway.security.CustomUserDetailsService;
import com.ecommerce.apigateway.security.jwt.TokenHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements WebFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

  private final TokenHelper jwtTokenHelper;

  private final CustomUserDetailsService customUserDetailsService;

  /**
   * @param jwtTokenHelper to retrieve details related to JWT
   * @param customUserDetailsService to load the user based on username
   */
  public AuthenticationFilter(
      TokenHelper jwtTokenHelper, CustomUserDetailsService customUserDetailsService) {

    this.jwtTokenHelper = jwtTokenHelper;
    this.customUserDetailsService = customUserDetailsService;
  }

  private static final String AUTHORIZATION = "Authorization";

  /**
   * @param exchange the current server exchange
   * @param chain provides a way to delegate to the next filter
   * @return
   */
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

    String requestToken = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION);

    String token;

    if (requestToken != null && requestToken.startsWith("Bearer")) {
      token = requestToken.substring(7);

      String username = this.jwtTokenHelper.extractUsername(token);

      if (jwtTokenHelper.validateToken(token, username)
          && SecurityContextHolder.getContext().getAuthentication() == null) {

        UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContext context = new SecurityContextImpl(authenticationToken);

        CustomUserDetails principal =
            (CustomUserDetails) context.getAuthentication().getPrincipal();

        ServerHttpRequest modifiedRequest =
            exchange
                .getRequest()
                .mutate()
                .header("username", principal.getUsername())
                .header("role", principal.getUser().getRole().name())
                .build();

        // Create a new ServerWebExchange with the modified request
        ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();

        return chain
            .filter(modifiedExchange)
            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
      } else {
        LOGGER.error("TOKEN IS MALFORMED OR EXPIRED");
      }
    } else {
      if (!(exchange.getRequest().getURI().getPath().contains("api-docs")
          || exchange.getRequest().getURI().getPath().contains("swagger-ui"))) {

        LOGGER.warn("TOKEN NOT FOUND");
      }
    }

    return chain.filter(exchange);
  }
}
