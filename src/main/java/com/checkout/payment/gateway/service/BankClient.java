package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.BankPaymentResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException.ServiceUnavailable;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class BankClient {

  private static final String BANK_PAYMENTS_URL = "http://localhost:8080/payments";

  private final RestTemplate restTemplate;

  public BankClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public BankPaymentResponse processPayment(BankPaymentRequest request) {
    try {
      return restTemplate.postForObject(
          BANK_PAYMENTS_URL,
          request,
          BankPaymentResponse.class
      );
    } catch (ServiceUnavailable | ResourceAccessException ex) {
      return null;
    }
  }

  /* Would think of this as a possibility where could add async calling of the bank. Did not complete.
  @Async
  public CompletableFuture<BankPaymentResponse> processPaymentAsync(BankPaymentRequest request) {
    try {
      BankPaymentResponse response = restTemplate.postForObject(
          BANK_PAYMENTS_URL,
          request,
          BankPaymentResponse.class
      );
      return CompletableFuture.completedFuture(response);
    } catch (ServiceUnavailable | ResourceAccessException ex) {
      return CompletableFuture.completedFuture(null);
    }
  } */
}
