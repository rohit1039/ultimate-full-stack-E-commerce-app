package com.ecommerce.orderservice.verticle;

import io.vertx.core.Promise;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MainVerticle extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class.getName());

  @Override
  public void start(Promise<Void> startPromise) {

    final Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create().setHandleFileUploads(true));
    vertx.rxDeployVerticle(new OrderVerticle());
    OrderVerticle.createOrder(router);
    vertx.createHttpServer()
         .requestHandler(router)
         .listen(8888)
         .doOnSuccess(success -> LOG.info("HTTP server started on port: 8888"))
         .doOnError(error -> {
           LOG.info("Server error occurred on port: 8888");
           startPromise.fail(error.getCause());
         })
         .subscribe();
  }

}
