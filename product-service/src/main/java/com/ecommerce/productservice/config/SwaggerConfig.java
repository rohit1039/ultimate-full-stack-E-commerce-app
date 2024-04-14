package com.ecommerce.productservice.config;

import static com.ecommerce.productservice.constants.Constants.API_GATEWAY_SERVER_URL;
import static com.ecommerce.productservice.constants.Constants.PRODUCT_SERVICE_SERVER_URL;

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

@Configuration
public class SwaggerConfig {

  /**
   * This method creates an instance of the OpenAPI class, which is a model that represents an
   * OpenAPI document. The components section of the OpenAPI object contains a list of components
   * used by the API, including security schemes. The security section of the OpenAPI object
   * contains a list of security requirements, which are a list of security schemes that apply to a
   * specific API operation. The info section of the OpenAPI object contains metadata about the API,
   * including the title, description, version, and license. The externalDocs section of the OpenAPI
   * object contains information about external documentation for the API.
   *
   * @return an instance of the OpenAPI class
   */
  @Bean
  public OpenAPI springOpenAPI() {

    SpringDocUtils.getConfig().addResponseTypeToIgnore(Links.class);
    return new OpenAPI()
        .components(
            new Components()
                .addSecuritySchemes(
                    "JWT_token",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .description("JWT Token Access")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization")))
        .security(Collections.singletonList(new SecurityRequirement().addList("JWT_token")))
        .info(
            new Info()
                .title("Product Service - Ultimate E-commerce Application")
                .description(
                    "This service is developed by Rohit Parida, managed by "
                        + "admins and consumed by the clients. "
                        + "This service is running"
                        + " on port 8083.")
                .version("v1.0.0")
                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
        .externalDocs(
            new ExternalDocumentation()
                .description("E-Commerce Application " + "Documentation")
                .url("https://mailchimp" + ".com/developer/marketing/docs/e" + "-commerce/"))
        .addServersItem(new Server().url(API_GATEWAY_SERVER_URL).description("API Gateway URL"))
        .addServersItem(
            new Server().url(PRODUCT_SERVICE_SERVER_URL).description("Generated server url"));
  }
}
