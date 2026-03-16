package com.checkout.payment.gateway.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.GetPaymentResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentMapperTest {

  private PaymentMapper paymentMapper;

  @BeforeEach
  void setUp() {
    paymentMapper = new PaymentMapper();
  }

  @Test
  void toBankRequest_shouldMapPostPaymentRequestToBankPaymentRequest() {
    PostPaymentRequest request = new PostPaymentRequest();
    request.setCardNumber("2222405343248877");
    request.setExpiryMonth(4);
    request.setExpiryYear(2028);
    request.setCurrency("GBP");
    request.setAmount(100);
    request.setCvv("123");

    BankPaymentRequest result = paymentMapper.toBankRequest(request);

    assertThat(result).isNotNull();
    assertThat(result.getCardNumber()).isEqualTo("2222405343248877");
    assertThat(result.getExpiryDate()).isEqualTo("4/2028");
    assertThat(result.getCurrency()).isEqualTo("GBP");
    assertThat(result.getAmount()).isEqualTo(100);
    assertThat(result.getCvv()).isEqualTo("123");
  }

  @Test
  void toPostPaymentResponse_shouldMapValuesCorrectly() {
    UUID paymentId = UUID.randomUUID();

    PostPaymentRequest request = new PostPaymentRequest();
    request.setExpiryMonth(12);
    request.setExpiryYear(2099);
    request.setCurrency("USD");
    request.setAmount(250);

    PostPaymentResponse result = paymentMapper.toPostPaymentResponse(
        paymentId,
        PaymentStatus.AUTHORIZED,
        request,
        8877
    );

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(paymentId);
    assertThat(result.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(result.getCardNumberLastFour()).isEqualTo(8877);
    assertThat(result.getExpiryMonth()).isEqualTo(12);
    assertThat(result.getExpiryYear()).isEqualTo(2099);
    assertThat(result.getCurrency()).isEqualTo("USD");
    assertThat(result.getAmount()).isEqualTo(250);
  }

  @Test
  void toGetPaymentResponse_shouldMapStoredPaymentToGetPaymentResponse() {
    UUID paymentId = UUID.randomUUID();

    PostPaymentResponse storedPayment = new PostPaymentResponse();
    storedPayment.setId(paymentId);
    storedPayment.setStatus(PaymentStatus.DECLINED);
    storedPayment.setCardNumberLastFour(4321);
    storedPayment.setExpiryMonth(7);
    storedPayment.setExpiryYear(2030);
    storedPayment.setCurrency("EUR");
    storedPayment.setAmount(999);

    GetPaymentResponse result = paymentMapper.toGetPaymentResponse(storedPayment);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(paymentId);
    assertThat(result.getStatus()).isEqualTo(PaymentStatus.DECLINED);
    assertThat(result.getCardNumberLastFour()).isEqualTo(4321);
    assertThat(result.getExpiryMonth()).isEqualTo(7);
    assertThat(result.getExpiryYear()).isEqualTo(2030);
    assertThat(result.getCurrency()).isEqualTo("EUR");
    assertThat(result.getAmount()).isEqualTo(999);
  }
}