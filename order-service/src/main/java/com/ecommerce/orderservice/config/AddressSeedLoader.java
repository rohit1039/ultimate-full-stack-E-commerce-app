package com.ecommerce.orderservice.config;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

/**
 * The AddressSeedLoader class is responsible for seeding address metadata into a MongoDB
 * collection. It reads data from a CSV file, transforms it into JSON objects, and inserts
 * them into a specified collection in the database.
 *
 * This class implements the CommandLineRunner interface, meaning it executes its logic
 * when the application starts running in a command-line environment.
 */
public class AddressSeedLoader implements CommandLineRunner {

  private static final Logger LOG = LoggerFactory.getLogger(AddressSeedLoader.class.getName());
  private final MongoClient mongoClient = ConfigLoader.mongoConfig();
  private static final String COLLECTION = "pincode_data";

  /**
   * Reads user address metadata from a CSV file, transforms each record into a JSON object,
   * and inserts them into a MongoDB collection. If the collection already contains data, it skips the import.
   *
   * @param args Optional command-line arguments. Not used in the current implementation.
   */
  @Override
  public void run(String... args) {

    String path = "src/main/resources/data/user_address_metadata.csv";
    List<JsonObject> documents = new ArrayList<>();
    LOG.info("Address seeding started...");
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] fields = line.split(",");
        if (fields.length >= 9 && !fields[4].equalsIgnoreCase("pincode")) {
          JsonObject doc = new JsonObject()
              .put("pincode", Long.valueOf(fields[4].trim()))
              .put("district", fields[7].trim())
              .put("state", fields[8].trim());
          documents.add(doc);
        }
      }
    } catch (IOException e) {
      LOG.error("Failed to load CSV: {}", e.getMessage());
    }

    mongoClient.rxFind(COLLECTION, new JsonObject())
        .flatMap(existing -> {
          if (existing.isEmpty()) {
            return Observable.fromIterable(documents)
                .concatMapSingle(doc -> mongoClient.rxInsert(COLLECTION, doc).toSingle())
                .toList();
          } else {
            LOG.info("Address metadata already present. Skipping import");
            return Single.just(Collections.emptyList());
          }
        }).doOnError(
            error -> System.err.println("Error: " + error.getMessage())).subscribe();
    LOG.info("Address seeding completed successfully!");
  }
}