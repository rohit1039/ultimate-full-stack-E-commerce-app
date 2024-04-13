package com.ecommerce.orderservice;

import com.ecommerce.orderservice.verticle.OrderVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class.getName());

  public static void main(String[] args) {

    var vertx = Vertx.vertx();
    vertx.exceptionHandler(exception -> LOG.error("Unhandled Exception: ", exception.getCause()));
    vertx.deployVerticle(new MainVerticle(), asyncResult -> {
      if (asyncResult.failed()) {
        LOG.error("Failed to deploy: ", asyncResult.cause());
      }
      LOG.info("Deployed verticle: {} Successfully!", MainVerticle.class.getName());
    });
  }

  @Override
  public void start(Promise<Void> startPromise) {

    final Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create().setHandleFileUploads(true));
    LOG.info("Deployed verticle: {} Successfully!", OrderVerticle.class.getName());
    vertx.deployVerticle(new OrderVerticle());
    OrderVerticle.createOrder(router);
    vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        LOG.info("HTTP server started on port 8888");
      }
      else {
        startPromise.fail(http.cause());
      }
    });
  }
}
