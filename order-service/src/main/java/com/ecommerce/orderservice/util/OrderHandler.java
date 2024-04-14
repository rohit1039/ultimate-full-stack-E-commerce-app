package com.ecommerce.orderservice.util;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderHandler implements Handler<RoutingContext> {

	private static final Logger LOG = LoggerFactory.getLogger(OrderHandler.class.getName());

	private static final WebClient vertxClient = WebClient.create(Vertx.currentContext().owner());

	@Override
	public void handle(RoutingContext routingContext) {

		JsonObject requestBody = routingContext.body().asJsonObject();
		JsonObject response = new JsonObject();
		long productId = Long.parseLong(routingContext.request().getParam("productId"));
		MultiMap entries = routingContext.request().params();
		LOG.info("*** Calling product-service with productId: {} ***", productId);
		vertxClient.put(8083, "localhost", "/products/v1/order-product/" + productId)
			.addQueryParam("quantity", entries.get("quantity"))
			.addQueryParam("productSize", entries.get("productSize"))
			.rxSend()
			.doOnSuccess(apiResponse -> {
				if (apiResponse.statusCode() == 200) {
					response.put("response", "Order placed successfully!");
					routingContext.response()
						.setStatusCode(201)
						.putHeader("content-type", "application/json")
						.end(response.encodePrettily());
				}
				else {
					routingContext.response()
						.setStatusCode(apiResponse.statusCode())
						.putHeader("content-type", "application/json")
						.end();
					LOG.error("Some error occurred in Product Service: {}", apiResponse.statusCode());
				}
				LOG.info("\n Path: {} responds with: {}", routingContext.normalizedPath(), response.encode());
			})
			.doOnError(error -> LOG.error("Some error occurred while calling product-service: " + error.getMessage()))
			.subscribe(
					message -> LOG.info("\n Product Service responded with: {} \n", message.bodyAsJsonObject() != null
							? message.bodyAsJsonObject().encodePrettily() : message.statusCode()));
		LOG.info("\n Incoming request: {}", requestBody.encode());
	}

}
