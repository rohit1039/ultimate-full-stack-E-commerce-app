package com.ecommerce.userservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Links;

import java.util.Collections;

import static com.ecommerce.userservice.util.Constants.API_GATEWAY_SERVER_URL;
import static com.ecommerce.userservice.util.Constants.USER_SERVICE_SERVER_URL;

/**
 * This class is used to configure the swagger documentation for the user service.
 */
@Configuration
public class SwaggerConfig {

	/**
	 * This method creates a new instance of the OpenAPI object, which is used to define
	 * the swagger documentation.
	 * @return a new instance of the OpenAPI object
	 */
	@Bean
	public OpenAPI springOpenAPI() {

		SpringDocUtils.getConfig().addResponseTypeToIgnore(Links.class);

		return new OpenAPI()
			.components(new Components().addSecuritySchemes("JWT_token",
					new SecurityScheme().type(SecurityScheme.Type.APIKEY)
						.description("JWT Token Access")
						.in(SecurityScheme.In.HEADER)
						.name("Authorization")))
			.security(Collections.singletonList(new SecurityRequirement().addList("JWT_token")))
			.info(new Info().title("User Service - Ultimate E-commerce Application")
				.description(
						"This service is developed by Rohit Parida, managed by admins and consumed by the clients. "
								+ "This service is running on port 8080.")
				.version("v1.0.0")
				.license(new License().name("Apache 2.0").url("http://springdoc.org")))
			.externalDocs(new ExternalDocumentation().description("E-Commerce Application Documentation")
				.url("https://mailchimp.com/developer/marketing/docs/e-commerce/"))
			.addServersItem(new Server().url(API_GATEWAY_SERVER_URL).description("API Gateway URL"))
			.addServersItem(new Server().url(USER_SERVICE_SERVER_URL).description("Generated server url"));
	}

}
