package com.ecommerce.productservice;

import com.ecommerce.productservice.exception.RestTemplateExceptionHandler;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@SpringBootApplication
@EnableCaching
public class ProductServiceApplication {

  @Autowired private CacheManager cacheManager;

  public static void main(String[] args) {

    SpringApplication.run(ProductServiceApplication.class, args);
  }

  @Bean
  public ModelMapper modelMapper() {

    return new ModelMapper();
  }

  @Bean
  @LoadBalanced
  public RestTemplate restTemplate() {

    return new RestTemplateBuilder().errorHandler(new RestTemplateExceptionHandler()).build();
  }

  @Bean
  public FilterRegistrationBean<?> coresFilter() {

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
    FilterRegistrationBean<?> bean = new FilterRegistrationBean<>(new CorsFilter(source));
    bean.setOrder(-110);
    return bean;
  }
}
