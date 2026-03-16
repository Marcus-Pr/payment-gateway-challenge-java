package com.checkout.payment.gateway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.BankPaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class BankClientTest {

  @Mock
  private RestTemplate restTemplate;

  private BankClient bankClient;

  @BeforeEach
  void setUp() {
    bankClient = new BankClient(restTemplate);
  }

  @Test
  void processPayment_shouldReturnResponse_whenBankCallSucceeds() {
    BankPaymentRequest request = new BankPaymentRequest();
    request.setCardNumber("2222405343248877");
    request.setExpiryDate("04/2028");
    request.setCurrency("GBP");
    request.setAmount(100);
    request.setCvv("123");

    BankPaymentResponse bankResponse = new BankPaymentResponse();
    bankResponse.setAuthorized(true);
    bankResponse.setAuthorizationCode("auth-code");

    when(restTemplate.postForObject(
        eq("http://localhost:8080/payments"),
        eq(request),
        eq(BankPaymentResponse.class)
    )).thenReturn(bankResponse);

    BankPaymentResponse result = bankClient.processPayment(request);

    assertThat(result).isNotNull();
    assertThat(result.isAuthorized()).isTrue();
    assertThat(result.getAuthorizationCode()).isEqualTo("auth-code");
  }

  @Test
  void processPayment_shouldReturnNull_whenBankReturnsServiceUnavailable() {
    BankPaymentRequest request = new BankPaymentRequest();

    when(restTemplate.postForObject(
        eq("http://localhost:8080/payments"),
        eq(request),
        eq(BankPaymentResponse.class)
    )).thenThrow(HttpServerErrorException.create(
        "Service unavailable",
        HttpStatus.SERVICE_UNAVAILABLE,
        "Service unavailable",
        HttpHeaders.EMPTY,
        new byte[0],
        null
    ));

    BankPaymentResponse result = bankClient.processPayment(request);

    assertThat(result).isNull();
  }

  @Test
  void processPayment_shouldReturnNull_whenBankCannotBeReached() {
    BankPaymentRequest request = new BankPaymentRequest();

    when(restTemplate.postForObject(
        eq("http://localhost:8080/payments"),
        eq(request),
        eq(BankPaymentResponse.class)
    )).thenThrow(new ResourceAccessException("Connection refused"));

    BankPaymentResponse result = bankClient.processPayment(request);

    assertThat(result).isNull();
  }
}