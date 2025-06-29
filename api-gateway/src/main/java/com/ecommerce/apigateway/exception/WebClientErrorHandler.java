package com.ecommerce.apigateway.exception;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

public class WebClientErrorHandler {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Creates an {@code ExchangeFilterFunction} to handle HTTP errors in a reactive web client.
   *
   * <p>This filter processes the HTTP responses returned from the server and applies custom error
   * handling based on the status codes: - For 4xx client errors, it invokes {@code
   * handleClientError} to process the error. - For 5xx server errors, it invokes {@code
   * handleServerError} to handle server-side issues. - For other successful responses, it passes
   * the response through unchanged.
   *
   * @return an {@code ExchangeFilterFunction} instance that processes responses to handle errors
   *     appropriately
   */
  public static ExchangeFilterFunction errorHandlingFilter() {
    return ExchangeFilterFunction.ofResponseProcessor(
        response -> {
          HttpStatusCode status = response.statusCode();
          if (status.is4xxClientError()) {
            return handleClientError(response);
          } else if (status.is5xxServerError()) {
            return handleServerError(response);
          } else {
            return Mono.just(response);
          }
        });
  }

  /**
   * Handles client-side errors (4xx HTTP status codes) by extracting the error body from the {@link
   * ClientResponse} and attempting to map it into a list of {@link ErrorResponse} objects. If the
   * parsing fails, a fallback {@link ErrorResponse} is created with the raw error body and a status
   * code of 500. This method ultimately raises a {@link ClientException} containing the error
   * details.
   *
   * @param response the client response with a 4xx status code
   * @return a {@link Mono} that emits a {@link ClientException} wrapping the parsed error details
   */
  private static Mono<ClientResponse> handleClientError(ClientResponse response) {
    return response
        .bodyToMono(String.class)
        .flatMap(
            errorBody -> {
              System.out.println(errorBody);
              try {
                List<ErrorResponse> errors =
                    objectMapper.readValue(errorBody, new TypeReference<List<ErrorResponse>>() {});
                return Mono.error(new ClientException(errors));
              } catch (Exception e) {
                ErrorResponse fallback = new ErrorResponse();
                fallback.setErrorMessage(e.getLocalizedMessage());
                fallback.setDeveloperMessage(errorBody);
                return Mono.error(new ClientException(List.of(fallback)));
              }
            });
  }

  /**
   * Processes server error responses and constructs a {@link Mono} that represents an error signal.
   * This method extracts the error body from the response and wraps it in a {@link
   * RuntimeException}.
   *
   * @param response the {@link ClientResponse} containing the server error details
   * @return a {@link Mono} that emits a {@link RuntimeException} with the error message
   */
  private static Mono<ClientResponse> handleServerError(ClientResponse response) {
    return response
        .bodyToMono(String.class)
        .flatMap(errorBody -> Mono.error(new RuntimeException("Server error: " + errorBody)));
  }
}
