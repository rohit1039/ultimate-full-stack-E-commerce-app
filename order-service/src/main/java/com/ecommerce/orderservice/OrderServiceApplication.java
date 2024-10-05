package com.ecommerce.orderservice;

import com.ecommerce.orderservice.verticle.MainVerticle;
import io.vertx.rxjava3.core.Vertx;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderServiceApplication {

  private static final Logger LOG = LoggerFactory.getLogger(OrderServiceApplication.class.getName());

  private final MainVerticle mainVerticle;

  public OrderServiceApplication(MainVerticle mainVerticle) {

    this.mainVerticle = mainVerticle;
  }

  public static void main(String[] args) {

    SpringApplication.run(OrderServiceApplication.class);
    LOG.info("Server started on port: 8084");
  }

  @PostConstruct
  public void deployServerVerticle() {

    var vertx = Vertx.vertx();
    vertx.exceptionHandler(exception -> LOG.error("Unhandled Exception: ", exception.getCause()));
    vertx.rxDeployVerticle(mainVerticle)
         .doOnSuccess(success -> LOG.info("Deployed verticle: {} Successfully!", mainVerticle.getClass().getName()))
         .doOnError(error -> LOG.error("Failed to deploy: ", error.getCause()))
         .subscribe();
    LOG.info("Deployed verticle: {} Successfully!", mainVerticle.getClass().getName());
  }
}
