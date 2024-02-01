package com.ecommerce.apigateway.validator;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * A class that contains a list of open API endpoints and a predicate to determine if a
 * request is secured.
 */
@Component
public class RouteValidator {

	/**
	 * A list of open API endpoints that are not protected by authentication.
	 */
	public static final List<String> endPoints = List.of("/auth", "/api-docs");

	/**
	 * A predicate that determines if a request is secured based on the endpoint and HTTP
	 * method. return true if the request is not protected by authentication, false
	 * otherwise
	 */
	public Predicate<ServerHttpRequest> isSecured = request -> endPoints.stream()
		.noneMatch(uri -> (request.getURI().getPath().contains(endPoints.get(0))
				&& (request.getMethod().matches(String.valueOf(HttpMethod.POST))
						|| request.getMethod().matches(String.valueOf(HttpMethod.PATCH))))
				|| (request.getURI().getPath().contains(endPoints.get(1))
						&& (request.getMethod().matches(String.valueOf(HttpMethod.GET)))));

}