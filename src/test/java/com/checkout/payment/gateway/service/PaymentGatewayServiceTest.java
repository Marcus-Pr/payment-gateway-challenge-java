package com.checkout.payment.gateway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.exception.ValidationException;
import com.checkout.payment.gateway.mapper.PaymentMapper;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.model.BankPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

  private PaymentsRepository paymentsRepository;

  @Mock
  private BankClient bankClient;

  private PaymentMapper paymentMapper;

  private PaymentGatewayService paymentGatewayService;

  private PostPaymentRequest validRequest;

  @BeforeEach
  void setUp() {
    paymentsRepository = new PaymentsRepository();
    paymentMapper = new PaymentMapper();
    paymentGatewayService = new PaymentGatewayService(paymentsRepository, bankClient, paymentMapper);
    validRequest = new PostPaymentRequest();
    validRequest.setCardNumber("2222405343248877");
    validRequest.setExpiryMonth(12);
    validRequest.setExpiryYear(2099);
    validRequest.setCurrency("GBP");
    validRequest.setAmount(100);
    validRequest.setCvv("123");
  }

  @Test
  void processPayment_shouldReturnAuthorized_whenBankAuthorizesPayment() {
    BankPaymentResponse bankResponse = new BankPaymentResponse();
    bankResponse.setAuthorized(true);
    bankResponse.setAuthorizationCode("auth-code");

    when(bankClient.processPayment(any())).thenReturn(bankResponse);

    PostPaymentResponse response = paymentGatewayService.processPayment(validRequest);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isNotNull();
    assertThat(response.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(response.getCardNumberLastFour()).isEqualTo(8877);
    assertThat(response.getExpiryMonth()).isEqualTo(12);
    assertThat(response.getExpiryYear()).isEqualTo(2099);
    assertThat(response.getCurrency()).isEqualTo("GBP");
    assertThat(response.getAmount()).isEqualTo(100);
  }

  @Test
  void processPayment_shouldReturnDeclined_whenBankDeclinesPayment() {
    BankPaymentResponse bankResponse = new BankPaymentResponse();
    bankResponse.setAuthorized(false);

    when(bankClient.processPayment(any()))
        .thenReturn(bankResponse);

    PostPaymentResponse response = paymentGatewayService.processPayment(validRequest);

    assertThat(response.getStatus()).isEqualTo(PaymentStatus.DECLINED);
    assertThat(response.getCardNumberLastFour()).isEqualTo(8877);
  }

  @Test
  void processPayment_shouldReturnRejected_whenBankResponseIsNull() {
    when(bankClient.processPayment(any()))
        .thenReturn(null);

    PostPaymentResponse response = paymentGatewayService.processPayment(validRequest);

    assertThat(response.getStatus()).isEqualTo(PaymentStatus.REJECTED);
  }

  @Test
  void processPayment_shouldThrowException_whenCardIsExpired() {
    validRequest.setExpiryMonth(1);
    validRequest.setExpiryYear(2020);

    assertThatThrownBy(() -> paymentGatewayService.processPayment(validRequest))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Card has expired");
  }

  @Test
  void getPaymentById_shouldReturnPayment_whenPaymentExists() {
    UUID id = UUID.randomUUID();

    PostPaymentResponse storedPayment = new PostPaymentResponse();
    storedPayment.setId(id);
    storedPayment.setStatus(PaymentStatus.AUTHORIZED);
    storedPayment.setCardNumberLastFour(8877);
    storedPayment.setExpiryMonth(12);
    storedPayment.setExpiryYear(2099);
    storedPayment.setCurrency("GBP");
    storedPayment.setAmount(100);

    paymentsRepository.add(storedPayment);

    var response = paymentGatewayService.getPaymentById(id);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(id);
    assertThat(response.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(response.getCardNumberLastFour()).isEqualTo(8877);
  }

  @Test
  void getPaymentById_shouldThrowException_whenPaymentDoesNotExist() {
    UUID id = UUID.randomUUID();

    assertThatThrownBy(() -> paymentGatewayService.getPaymentById(id))
        .isInstanceOf(EventProcessingException.class)
        .hasMessage("Invalid ID");
  }
}