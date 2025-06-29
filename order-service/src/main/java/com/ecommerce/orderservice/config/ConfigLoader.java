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

/**
 * The ConfigLoader class provides a configuration utility for establishing a connection
 * to a MongoDB database and setting up an index for a collection.
 * It is used to configure and create a shared MongoClient instance with predefined
 * database connection settings.
 *
 * This class ensures that a MongoClient is properly configured with a specific database URI
 * and database name, and that an index with unique constraints is created for the given collection.
 */
public class ConfigLoader {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);
  private static final Vertx vertx = Vertx.currentContext().owner();
  private static final String DB_URI = "mongodb://localhost:27017";
  private static final String DB_NAME = "orderDB";

  /**
   * Configures and initializes a shared MongoClient instance for interacting with a MongoDB database.
   * The configuration includes setting up the database connection string, database name,
   * and creating a unique index for a collection.
   *
   * @return a configured instance of MongoClient for interacting with the MongoDB database.
   */
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
