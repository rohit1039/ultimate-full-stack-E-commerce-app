package com.ecommerce.apigateway.config;

import com.ecommerce.apigateway.filter.AuthenticationFilter;
import com.ecommerce.apigateway.security.jwt.AuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

  private final AuthenticationEntryPoint authenticationEntryPoint;

  private final AuthenticationFilter authenticationFilter;

  /**
   * This SecurityFilterChain is used to allow or restrict users in order to access certain
   * resources
   *
   * @param http to get http requests
   * @return chain of authorized http requests
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
        .pathMatchers(HttpMethod.GET, "/users/v3/api-docs/**")
        .permitAll()
        .pathMatchers("/users/**")
        .hasRole("ADMIN")
        .pathMatchers(HttpMethod.POST, "/products/**")
        .hasRole("ADMIN")
        .pathMatchers(HttpMethod.PUT, "/products/**")
        .hasRole("ADMIN")
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
   * This PasswordEncoder is used to encrypt user's password
   *
   * @return BCryptPasswordEncoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {

    return new BCryptPasswordEncoder();
  }

  /**
   * This method is used to configure CORs policy with client i.e. React
   *
   * @return allowed CORs origin http methods
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
