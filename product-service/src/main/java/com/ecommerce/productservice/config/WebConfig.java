package com.ecommerce.productservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class WebConfig implements WebMvcConfigurer {

  @Bean
  public ObjectMapper objectMapper() {
    // Create a new ObjectMapper with snake_case property naming strategy
    ObjectMapper mapper = new ObjectMapper();
    mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    mapper.writerWithDefaultPrettyPrinter();
    mapper.registerModule(new JavaTimeModule());
    mapper.registerSubtypes(Links.class, Link.class);
    return mapper;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Register a resource handler for serving product images
    registry
        .addResourceHandler("/product-images/**")
        .addResourceLocations("file:" + System.getProperty("product.dir") + "/product-images/");
  }
}
