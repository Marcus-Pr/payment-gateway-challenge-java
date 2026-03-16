package com.checkout.payment.gateway.model;

import java.util.List;

public class ErrorResponse {

  private final String message;
  private final List<String> errors;

  public ErrorResponse(String message) {
    this.message = message;
    this.errors = null;
  }

  public ErrorResponse(List<String> errors) {
    this.message = "Validation failed";
    this.errors = errors;
  }

  public String getMessage() {
    return message;
  }

  public List<String> getErrors() {
    return errors;
  }

  /* @Override
  public String toString() {
    return "ErrorResponse{" +
        "message='" + message + '\'' +
        '}';
  }*/
}
