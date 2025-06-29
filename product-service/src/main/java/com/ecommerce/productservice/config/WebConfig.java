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

  /**
   * Provides an instance of {@link ObjectMapper} configured with specific settings: - Uses
   * snake_case for property naming. - Registers the {@link JavaTimeModule} for handling Java 8 date
   * and time types. - Enables pretty-printing for JSON serialization. - Registers subtypes for
   * {@link Links} and {@link Link} classes.
   *
   * @return a configured {@link ObjectMapper} instance.
   */
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

  /**
   * Configures resource handlers to serve static resources such as product images.
   *
   * <p>This method maps specific URL paths to corresponding resource locations on the file system,
   * enabling the application to serve static files directly.
   *
   * @param registry the {@code ResourceHandlerRegistry} instance used to register resource mappings
   */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Register a resource handler for serving product images
    registry
        .addResourceHandler("/product-images/**")
        .addResourceLocations("file:" + System.getProperty("product.dir") + "/product-images/");
  }
}
