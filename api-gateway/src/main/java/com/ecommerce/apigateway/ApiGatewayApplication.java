package com.ecommerce.apigateway;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication(exclude = {ReactiveUserDetailsServiceAutoConfiguration.class})
@EnableAsync
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class ApiGatewayApplication {

  public static void main(String[] args) {

    SpringApplication.run(ApiGatewayApplication.class);
  }

  @Bean
  @LoadBalanced
  public WebClient.Builder webClient() {

    return WebClient.builder();
  }

  @Bean
  public ModelMapper modelMapper() {

    return new ModelMapper();
  }
}
