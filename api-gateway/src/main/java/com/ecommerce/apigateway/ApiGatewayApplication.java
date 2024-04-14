package com.ecommerce.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
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
}
