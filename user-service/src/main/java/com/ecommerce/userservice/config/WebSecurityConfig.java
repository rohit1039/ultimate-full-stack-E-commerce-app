package com.ecommerce.userservice.config;

import com.ecommerce.userservice.security.CustomUserDetailsService;
import com.ecommerce.userservice.security.jwt.JwtAuthenticationEntryPoint;
import com.ecommerce.userservice.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebSecurity
@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class WebSecurityConfig {

  public static final String DEFAULT_ADMIN_USER = "rohitparida0599@gmail.com";
  private final CustomUserDetailsService customUserDetailsService;
  private final JwtAuthenticationEntryPoint authenticationEntryPoint;
  private final JwtAuthenticationFilter authenticationFilter;

  /**
   * This SecurityFilterChain is used to allow or restrict users in order to access certain
   * resources
   *
   * @param http to get http requests
   * @return chain of authorized http requests
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http.cors()
        .and()
        .csrf()
        .disable()
        .authorizeHttpRequests()
        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**")
        .permitAll()
        .requestMatchers(HttpMethod.POST, "/v1/auth/**")
        .permitAll()
        .requestMatchers(HttpMethod.PATCH, "/v1/auth/**")
        .permitAll()
        .requestMatchers(HttpMethod.PATCH, "/users/**")
        .permitAll()
        .requestMatchers(HttpMethod.GET, "/v1/all", "/v1/get/**", "/v1/export/**")
        .hasRole("ADMIN")
        .requestMatchers(HttpMethod.GET, "/v1/current/role")
        .permitAll()
        .requestMatchers(HttpMethod.DELETE)
        .hasRole("ADMIN")
        .requestMatchers(HttpMethod.PATCH)
        .hasRole("ADMIN")
        .anyRequest()
        .fullyAuthenticated()
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(authenticationEntryPoint)
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .authenticationProvider(daoAuthenticationProvider());
    return http.build();
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

  public DaoAuthenticationProvider daoAuthenticationProvider() {

    DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
    daoAuthenticationProvider.setUserDetailsService(customUserDetailsService);
    daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
    return daoAuthenticationProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {

    return authenticationConfiguration.getAuthenticationManager();
  }

  /**
   * This method is used to configure CORs policy with client i.e. React
   *
   * @return allowed CORs origin http methods
   */
  @Bean
  public FilterRegistrationBean coresFilter() {

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
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
    source.registerCorsConfiguration("/**", corsConfiguration);
    FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
    bean.setOrder(-110);
    return bean;
  }
}
