package com.ecommerce.apigateway.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Collections;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Links;

@Configuration
public class OpenApiConfig {

  /**
   * This method creates an instance of the OpenAPI class, which is a model that represents an
   * OpenAPI document. The components section of the OpenAPI object contains a list of components
   * used by the API, including security schemes. The security section of the OpenAPI object
   * contains a list of security requirements, which are a list of security schemes that apply to a
   * specific API operation. The info section of the OpenAPI object contains general information
   * about the API, such as its title, description, and version. The externalDocs section of the
   * OpenAPI object contains information about external documentation for the API.
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
                .title("API Gateway - Ultimate E-Commerce Application")
                .description(
                    "This service is developed by Rohit Parida, managed by "
                        + "admins and consumed by the clients."
                        + "This service is running "
                        + "on port 8081.")
                .version("v1.0.0")
                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
        .externalDocs(
            new ExternalDocumentation()
                .description("E-Commerce Application " + "Documentation")
                .url("https://spring.wiki.github.org/docs"));
  }
}
