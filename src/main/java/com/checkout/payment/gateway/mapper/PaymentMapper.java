package com.checkout.payment.gateway.mapper;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.GetPaymentResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

  public PostPaymentResponse toPostPaymentResponse(
      UUID paymentId,
      PaymentStatus status,
      PostPaymentRequest request,
      int lastFourDigits) {

    PostPaymentResponse response = new PostPaymentResponse();

    response.setId(paymentId);
    response.setStatus(status);
    response.setCardNumberLastFour(lastFourDigits);
    response.setExpiryMonth(request.getExpiryMonth());
    response.setExpiryYear(request.getExpiryYear());
    response.setCurrency(request.getCurrency());
    response.setAmount(request.getAmount());

    return response;
  }

  public GetPaymentResponse toGetPaymentResponse(PostPaymentResponse storedPayment) {

    GetPaymentResponse response = new GetPaymentResponse();

    response.setId(storedPayment.getId());
    response.setStatus(storedPayment.getStatus());
    response.setCardNumberLastFour(storedPayment.getCardNumberLastFour());
    response.setExpiryMonth(storedPayment.getExpiryMonth());
    response.setExpiryYear(storedPayment.getExpiryYear());
    response.setCurrency(storedPayment.getCurrency());
    response.setAmount(storedPayment.getAmount());

    return response;
  }

  public BankPaymentRequest toBankRequest(PostPaymentRequest paymentRequest) {
    BankPaymentRequest bankRequest = new BankPaymentRequest();
    bankRequest.setCardNumber(paymentRequest.getCardNumber());
    bankRequest.setExpiryDate(paymentRequest.getExpiryDate());
    bankRequest.setCurrency(paymentRequest.getCurrency());
    bankRequest.setAmount(paymentRequest.getAmount());
    bankRequest.setCvv(paymentRequest.getCvv());
    return bankRequest;
  }
}