package com.ecommerce.categoryservice.exception;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class RestTemplateExceptionHandler implements ResponseErrorHandler {

  @Override
  public boolean hasError(ClientHttpResponse httpResponse) throws IOException {

    return (httpResponse.getStatusCode().is4xxClientError()
        || httpResponse.getStatusCode().is5xxServerError());
  }

  @Override
  public void handleError(ClientHttpResponse httpResponse) throws IOException {

    if (httpResponse.getStatusCode().is4xxClientError()
        || httpResponse.getStatusCode().is5xxServerError()) {
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(httpResponse.getBody()))) {
        String errorMessage = reader.lines().collect(Collectors.joining(""));
        throw new ClientException(
            httpResponse.getStatusCode().value(), errorMessage, httpResponse.getStatusText());
      }
    }
  }
}
