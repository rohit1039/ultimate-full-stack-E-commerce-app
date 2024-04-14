package com.ecommerce.orderservice.verticle;

import com.ecommerce.orderservice.util.OrderHandler;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderVerticle extends AbstractVerticle {

	private static final Logger LOG = LoggerFactory.getLogger(OrderVerticle.class.getName());

	public static void placeOrder(Router parentRoute) {

		LOG.info("Deployed verticle: {} Successfully!", OrderVerticle.class.getName());
		parentRoute.post("/v1/place-order/:productId").handler(new OrderHandler());
	}

}
