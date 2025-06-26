package com.ecommerce.cartservice.config;

import static com.ecommerce.cartservice.constants.Constants.API_GATEWAY_SERVER_URL;
import static com.ecommerce.cartservice.constants.Constants.CART_SERVICE_SERVER_URL;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collections;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Links;
import org.springframework.http.converter.json.MappingJacksonValue;

/**
 * This class is used to configure the Swagger documentation for the API. It defines the information
 * about the API, including its title, version, description, and license. It also defines the
 * security scheme for the API, which is JWT token authentication.
 */
@Configuration
public class SwaggerConfig {

  /**
   * This method creates a new instance of the OpenAPI class, which is used to define the Swagger
   * documentation. It sets the information about the API, including its title, version,
   * description, and license. It also sets the security scheme for the API, which is JWT token
   * authentication.
   *
   * @return an instance of the OpenAPI class
   */
  @Bean
  public OpenAPI springOpenAPI() {

    SpringDocUtils.getConfig().addResponseTypeToIgnore(Links.class);
    SpringDocUtils.getConfig().addResponseTypeToIgnore(MappingJacksonValue.class);
    SpringDocUtils.getConfig().addResponseTypeToIgnore(FilterProvider.class);
    return new OpenAPI().components(new Components().addSecuritySchemes("JWT_token",
                            new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                                                .description("JWT Token Access")
                                                .in(SecurityScheme.In.HEADER)
                                                .name("Authorization")))
                        .security(Collections.singletonList(
                            new SecurityRequirement().addList("JWT_token")))
                        .info(new Info().title("Cart Service - Ultimate E-commerce Application")
                                        .description(
                                            "This service is developed by Rohit Parida, managed " +
                                                "by " + "admins and consumed by the clients. " +
                                                "This service is running" + " on port 8086.")
                                        .version("v1.0.0")
                                        .license(new License().name("Apache 2.0")
                                                              .url("http://springdoc.org")))
                        .externalDocs(new ExternalDocumentation().description(
                                                                     "E-Commerce Application " +
                                                                         "Documentation")
                                                                 .url("https://mailchimp" +
                                                                     ".com/developer/marketing" +
                                                                     "/docs/e" + "-commerce/"))
                        .addServersItem(
                            new Server().url(API_GATEWAY_SERVER_URL).description("API Gateway URL"))
                        .addServersItem(new Server().url(CART_SERVICE_SERVER_URL)
                                                    .description("Generated server url"));
  }
}

