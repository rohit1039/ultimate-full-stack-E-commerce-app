package com.ecommerce.apigateway.controller;

import com.ecommerce.apigateway.model.Cart;
import com.ecommerce.apigateway.model.CartItem;
import com.ecommerce.apigateway.model.OrderItemRequest;
import com.ecommerce.apigateway.model.OrderRequest;
import com.ecommerce.apigateway.payload.request.CheckoutRequest;
import com.ecommerce.apigateway.payload.request.PaymentStatus;
import com.ecommerce.apigateway.payload.request.address.AddressRequest;
import com.ecommerce.apigateway.payload.response.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * This controller handles operations related to cart and checkout processes within the e-commerce
 * API gateway. It provides endpoints to manage cart items and to process orders by interacting with
 * external services such as cart-service and order-service.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Checkout Service", description = "Checkout and Cart Management APIs")
public class CheckoutController {

  private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutController.class);

  private final WebClient.Builder webClientBuilder;

  /**
   * Creates a map of headers to be used for HTTP requests.
   *
   * @param username the username to include in the headers
   * @param contactNumber the contact number to include in the headers
   * @param token the authorization token to include in the headers
   * @return a map containing the headers with keys "username", "Authorization", and "contact"
   */
  private static Map<String, String> createHeaders(
      String username, String contactNumber, String token) {
    Map<String, String> headers = new HashMap<>();
    headers.put("username", username);
    headers.put("Authorization", token);
    headers.put("contact", contactNumber);
    return headers;
  }

  /**
   * Processes the checkout request by interacting with the cart and order services. This method
   * retrieves the cart details, validates the cart status, creates an order, and clears the cart
   * after a successful order placement.
   *
   * @param username the username of the user initiating the checkout
   * @param contactNumber the contact number of the user initiating the checkout
   * @param token the authorization token for user authentication
   * @param request the request payload containing details required for checkout, such as address
   * @return a {@link Mono} containing the order response if the operation is successful, or an
   *     error response
   */
  @Operation(
      summary = "Process checkout",
      description =
          "Processes the checkout request by creating an order from cart items and clearing the cart")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successful checkout"),
        @ApiResponse(responseCode = "400", description = "Cart is empty"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping("/gateway/orders/checkout")
  public Mono<?> checkout(
      @Schema(hidden = true) @RequestHeader("username") String username,
      @Schema(hidden = true) @RequestHeader("contact") String contactNumber,
      @Schema(hidden = true) @RequestHeader("Authorization") String token,
      @RequestBody CheckoutRequest request) {

    WebClient webClient = webClientBuilder.build();

    return webClient
        .get()
        .uri("http://cart-service/cart/get")
        .headers(headers -> headers.setAll(createHeaders(username, contactNumber, token)))
        .retrieve()
        .bodyToMono(Cart.class)
        .flatMap(
            cart -> {
              if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                LOGGER.warn("Cart is empty for user: {}", username);
                return Mono.just(ResponseEntity.badRequest().body("Cart is empty"));
              }
              LOGGER.info("Cart is not empty, proceeding to place the order 🎉");
              List<OrderItemRequest> orderItems =
                  cart.getItems().stream()
                      .map(
                          item ->
                              new OrderItemRequest(
                                  item.getProductId(),
                                  item.getProductName(),
                                  item.getQuantity(),
                                  item.getSize(),
                                  item.getColor()))
                      .collect(Collectors.toList());
              OrderRequest orderRequest = new OrderRequest();
              orderRequest.setOrderItems(orderItems);
              AddressRequest address = request.getAddress();
              address.setAddressCreationDate(LocalDateTime.now());
              address.setLastUpdatedAddressDate(LocalDateTime.now());
              address.setUsername(username);
              address.setPhoneNumber(contactNumber);
              orderRequest.setAddress(address);
              return webClient
                  .post()
                  .uri("http://order-service/orders/place-order")
                  .headers(headers -> headers.setAll(createHeaders(username, contactNumber, token)))
                  .contentType(MediaType.APPLICATION_JSON)
                  .bodyValue(orderRequest)
                  .retrieve()
                  .onStatus(HttpStatusCode::is5xxServerError, response ->
                      response.bodyToMono(String.class).flatMap(errorBody ->
                          Mono.error(new RuntimeException("Order-service 500: " + errorBody))))
                  .bodyToMono(OrderResponse.class)
                  .flatMap(
                      orderResponse -> {
                        orderResponse.setOrderDate(LocalDateTime.now());
                        orderResponse.setPaymentStatus(PaymentStatus.PENDING);
                        orderResponse.setOrderItems(orderItems);
                        orderResponse.setUsername(username);
                        return webClient
                            .delete()
                            .uri("http://cart-service/cart/delete")
                            .headers(
                                headers ->
                                    headers.setAll(createHeaders(username, contactNumber, token)))
                            .retrieve()
                            .toBodilessEntity()
                            .thenReturn(orderResponse);
                      });
            });
  }

  /**
   * Adds items to the user's cart by forwarding the request to the cart-service.
   *
   * @param username the username of the user adding items to the cart
   * @param contactNumber the contact number of the user
   * @param token the authorization token for validating the user
   * @param items a list of items to be added to the cart
   * @return a {@link Mono} emitting the updated {@link Cart} object upon successful addition of
   *     items
   */
  @Operation(
      summary = "Add items to cart",
      description = "Adds one or more items to the user's shopping cart")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Items successfully added to cart"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping("/gateway/cart/add")
  public Mono<Cart> addToCart(
      @Schema(hidden = true) @RequestHeader("username") String username,
      @Schema(hidden = true) @RequestHeader("contact") String contactNumber,
      @Schema(hidden = true) @RequestHeader("Authorization") String token,
      @RequestBody List<CartItem> items) {

    WebClient webClient = webClientBuilder.build();

    return webClient
        .post()
        .uri("http://cart-service/cart/add")
        .headers(h -> h.setAll(createHeaders(username, contactNumber, token)))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(items)
        .retrieve()
        .bodyToMono(Cart.class);
  }
}
