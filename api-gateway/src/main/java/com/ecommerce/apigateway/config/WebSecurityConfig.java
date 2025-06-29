package com.ecommerce.apigateway.config;

import com.ecommerce.apigateway.filter.AuthenticationFilter;
import com.ecommerce.apigateway.security.CustomUserDetailsService;
import com.ecommerce.apigateway.security.jwt.AuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * The WebSecurityConfig class is a configuration class for setting up application-level security using Spring WebFlux.
 * It primarily defines security policies, authentication mechanisms, authorization rules, and cross-origin resource sharing (CORS) policies.
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

  private final AuthenticationEntryPoint authenticationEntryPoint;

  private final AuthenticationFilter authenticationFilter;

  private final CustomUserDetailsService userDetailsService;

  /**
   * Configures the Spring Security filter chain for the web application. This method specifies
   * the security rules for different API endpoints, including authentication and authorization
   * policies, exception handling, and additional security configurations.
   *
   * @param http the {@code ServerHttpSecurity} object used to customize the security settings
   *             for the application.
   * @return a configured {@code SecurityWebFilterChain} containing the security rules and filters
   *         for the application.
   */
  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

    return http.cors()
        .and()
        .csrf()
        .disable()
        .authorizeExchange()
        .pathMatchers("/v3/api-docs/**", "/webjars/swagger-ui/**")
        .permitAll()
        .pathMatchers("/webhook/**")
        .permitAll()
        .pathMatchers(HttpMethod.GET, "/users/v3/api-docs/**")
        .permitAll()
        .pathMatchers(HttpMethod.GET, "/cart/v3/api-docs/**")
        .permitAll()
        .pathMatchers(HttpMethod.GET, "/api/v3/api-docs/**")
        .permitAll()
        .pathMatchers("/users/**")
        .hasRole("ADMIN")
        .pathMatchers(HttpMethod.POST, "/products/**")
        .permitAll()
        .pathMatchers(HttpMethod.PUT, "/products/**")
        .permitAll()
        .pathMatchers(HttpMethod.PATCH, "/products/**")
        .hasRole("ADMIN")
        .pathMatchers(HttpMethod.GET, "/products/**")
        .permitAll()
        .pathMatchers(HttpMethod.POST, "/categories/**")
        .hasRole("ADMIN")
        .pathMatchers(HttpMethod.PUT, "/categories/**")
        .hasRole("ADMIN")
        .pathMatchers(HttpMethod.PATCH, "/categories/**")
        .hasRole("ADMIN")
        .pathMatchers(HttpMethod.GET, "/categories/**")
        .permitAll()
        .pathMatchers("/v1/auth/**")
        .permitAll()
        .pathMatchers("/gateway/**")
        .authenticated()
        .pathMatchers("/orders/place-order")
        .permitAll()
        .pathMatchers("/cart/**")
        .authenticated()
        .pathMatchers("/orders/get-orders")
        .permitAll()
        .pathMatchers("/orders/all")
        .hasRole("ADMIN")
        .pathMatchers("/orders/update-status")
        .permitAll()
        .pathMatchers(HttpMethod.PATCH, "/orders/**")
        .hasRole("ADMIN")
        .pathMatchers(HttpMethod.GET, "/payments/**")
        .permitAll()
        .pathMatchers(HttpMethod.GET, "/v1/all", "/v1/get/**", "/v1/export/**")
        .hasRole("ADMIN")
        .pathMatchers(HttpMethod.DELETE)
        .hasRole("ADMIN")
        .anyExchange()
        .authenticated()
        .and()
        .addFilterBefore(authenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .exceptionHandling()
        .authenticationEntryPoint(authenticationEntryPoint)
        .and()
        .build();
  }

  /**
   * Provides a bean of type {@link PasswordEncoder} for encoding and verifying passwords.
   *
   * @return an instance of {@link BCryptPasswordEncoder} to handle password encoding.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {

    return new BCryptPasswordEncoder();
  }

  /**
   * Creates and configures a ReactiveAuthenticationManager bean that uses a
   * UserDetailsRepositoryReactiveAuthenticationManager to authenticate users.
   * The authentication manager is set up with a password encoder to handle
   * password validation.
   *
   * @return a configured instance of ReactiveAuthenticationManager
   */
  @Bean
  public ReactiveAuthenticationManager authenticationManager() {
    UserDetailsRepositoryReactiveAuthenticationManager authenticationManager =
        new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);

    authenticationManager.setPasswordEncoder(passwordEncoder());

    return authenticationManager;
  }

  /**
   * Configures and provides a CORS filter bean to handle Cross-Origin Resource Sharing (CORS) configurations.
   * This method sets up the necessary CORS rules, such as allowing credentials, defining allowed origins, headers,
   * methods, and the maximum age for CORS preflight requests.
   *
   * @return a {@link CorsFilter} configured with the specified CORS settings.
   */
  @Bean
  public CorsFilter corsFilter() {

    CorsConfiguration corsConfiguration = new CorsConfiguration();

    corsConfiguration.setAllowCredentials(true);
    corsConfiguration.addAllowedOriginPattern("*");
    corsConfiguration.addAllowedHeader("Authorization");
    corsConfiguration.addAllowedHeader("Content-Type");
    corsConfiguration.addAllowedHeader("Accept");
    corsConfiguration.addAllowedMethod("POST");
    corsConfiguration.addAllowedMethod("GET");
    corsConfiguration.addAllowedMethod("DELETE");
    corsConfiguration.addAllowedMethod("PUT");
    corsConfiguration.addAllowedMethod("PATCH");
    corsConfiguration.addAllowedMethod("OPTIONS");
    corsConfiguration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfiguration);

    return new CorsFilter(source);
  }
}
