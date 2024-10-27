package com.ecommerce.orderservice.config;

import static com.ecommerce.orderservice.constant.ApiConstants.COLLECTION;
import static com.ecommerce.orderservice.constant.ApiConstants.ORDER_ID;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.IndexOptions;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigLoader {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);
  private static final Vertx vertx = Vertx.currentContext().owner();
  private static final String DB_URI = "mongodb://localhost:27017";
  private static final String DB_NAME = "orderDB";

  public static MongoClient mongoConfig() {

    final JsonObject mongoConfig =
        new JsonObject().put("connection_string", DB_URI).put("db_name", DB_NAME);
    LOG.info("MongoClient configured successfully!");
    JsonObject index = new JsonObject().put(ORDER_ID, UUID.randomUUID().toString());
    MongoClient mongoClient = MongoClient.createShared(vertx, mongoConfig);
    mongoClient.createIndexWithOptions(COLLECTION, index, new IndexOptions().unique(true));
    return mongoClient;
  }
}
