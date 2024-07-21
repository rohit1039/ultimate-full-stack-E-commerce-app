package com.ecommerce.orderservice.verticle;

import io.vertx.core.Promise;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;
import io.vertx.rxjava3.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MainVerticle extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class.getName());

  @Override
  public void start(Promise<Void> startPromise) {

    final Router router = Router.router(vertx);
    router.route("/*").handler(StaticHandler.create());
    router.route().handler(BodyHandler.create().setHandleFileUploads(true));
    vertx.deployVerticle(new OrderVerticle(router));
    LOG.info("Deployed verticle: {} Successfully!", OrderVerticle.class.getName());

    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(8084)
        .doOnError(
            error -> {
              LOG.info("Http Server error occurred on port: 8084");
              startPromise.fail(error.getCause());
            })
        .subscribe();
  }
}
