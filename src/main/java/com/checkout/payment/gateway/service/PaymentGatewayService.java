package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.exception.ValidationException;
import com.checkout.payment.gateway.mapper.PaymentMapper;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.BankPaymentResponse;
import com.checkout.payment.gateway.model.GetPaymentResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;

  private final BankClient bankClient;

  private final PaymentMapper paymentMapper;

  public PaymentGatewayService(PaymentsRepository paymentsRepository, BankClient bankClient,
      PaymentMapper paymentMapper) {
    this.paymentsRepository = paymentsRepository;
    this.bankClient = bankClient;
    this.paymentMapper = paymentMapper;
  }

  public GetPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    PostPaymentResponse storedPayment =
        paymentsRepository.get(id)
                          .orElseThrow(() -> new EventProcessingException("Invalid ID"));

    return paymentMapper.toGetPaymentResponse(storedPayment);
  }

  public PostPaymentResponse  processPayment(PostPaymentRequest paymentRequest) {
    UUID paymentId = UUID.randomUUID();

    LOG.info("Start payment {} process", paymentId);

    validatePaymentRequest(paymentRequest);

    BankPaymentRequest bankRequest = paymentMapper.toBankRequest(paymentRequest);
    BankPaymentResponse bankResponse = bankClient.processPayment(bankRequest);
    // Could not currently complete the bank req to be async.
    // CompletableFuture<BankPaymentResponse> future = bankClient.processPaymentAsync(bankRequest);
    // BankPaymentResponse bankResponse = future.get();

    PaymentStatus status = mapPaymentStatus(bankResponse);
    int lastFourDigits = getLastFourDigits(paymentRequest.getCardNumber());

    PostPaymentResponse response =
        paymentMapper.toPostPaymentResponse(
            paymentId,
            status,
            paymentRequest,
            lastFourDigits
        );

    paymentsRepository.add(response);

    LOG.info("Finish payment {} processed with status {}", paymentId, status);

    return response;
  }

  private PaymentStatus mapPaymentStatus(BankPaymentResponse bankResponse) {
    if (bankResponse == null) {
      return PaymentStatus.REJECTED;
    }

    return bankResponse.isAuthorized() ? PaymentStatus.AUTHORIZED
                                       : PaymentStatus.DECLINED;
  }

  private int getLastFourDigits(String cardNumber) {
    String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
    return Integer.parseInt(lastFourDigits);
  }

  private void validatePaymentRequest(PostPaymentRequest request) {
    List<String> errors = new ArrayList<>();

    validateExpiryDate(request, errors);

    if (!errors.isEmpty()) {
      throw new ValidationException(errors);
    }
  }

  private void validateExpiryDate(PostPaymentRequest request, List<String> errors) {
    if (request.getExpiryMonth() == null || request.getExpiryYear() == null) {
      return;
    }

    YearMonth now = YearMonth.now();
    YearMonth expiry = YearMonth.of(request.getExpiryYear(), request.getExpiryMonth());

    if (expiry.isBefore(now)) {
      errors.add("Card has expired");
    }
  }
}
