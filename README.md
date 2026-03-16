# Instructions for candidates

This is the Java version of the Payment Gateway challenge. If you haven't already read this [README.md](https://github.com/cko-recruitment/) on the details of this exercise, please do so now.

## Requirements
- JDK 17
- Docker

## Template structure

src/ - A skeleton SpringBoot Application

test/ - Some simple JUnit tests

imposters/ - contains the bank simulator configuration. Don't change this

.editorconfig - don't change this. It ensures a consistent set of rules for submissions when reformatting code

docker-compose.yml - configures the bank simulator


## API Documentation
For documentation openAPI is included, and it can be found under the following url: **http://localhost:8090/swagger-ui/index.html**

**Feel free to change the structure of the solution, use a different library etc.**

---

# Payment Gateway

A simple payment gateway API built with Spring Boot that allows merchants to process card payments 
and retrieve previously processed payments.

The gateway validates payment requests, forwards them to a simulated acquiring bank, 
stores the result, and returns the payment status to the merchant.

## Features

The payment gateway supports the following functionality:
* Process card payments
* Retrieve previously processed payments
* Validate payment requests
* Mask card details for security (only last four digits returned)
* Communicate with a simulated acquiring bank (started via `docker-compose up`)
* Store payment results in memory

Payment statuses returned by the system:
* Authorized - payment successfully authorized by the bank
* Declined - payment declined by the bank
* Rejected - request rejected due to invalid input or bank failure

## Architecture

```
Controller
↓
Service
↓
Repository
```

### Endpoints:
* ```POST /payment```
* ```GET /payment/{id}```

### Service
Contains business logic for processing and retrieving payments.

```PaymentGatewayService``` responsibilities:
* validate requests
* call acquiring bank
* determine payment status
* store payments

### Repository
Stores payment results in memory.

```PaymentsRepository```
uses a thread-safe:
* ```ConcurrentHashMap<UUID, PostPaymentResponse>```

### Mapper

Handles conversion between internal models and API models.

```PaymentMapper```
example mappings:
* PostPaymentRequest -> BankPaymentRequest
* PostPaymentResponse -> GetPaymentResponse

### Bank Client

Handles communication with the acquiring bank simulator.

`BankClient` uses `RestTemplate` to call the bank simulator.

## Running the Application
### Start the Bank Simulator

The acquiring bank simulator must be running.

Start it with:

```bash
docker-compose up
```
The simulator will run on:
```
http://localhost:8080/payments
```

### Start the Gateway

Run the Spring Boot application:

```bash
./gradlew bootRun
```
The gateway runs on:
```
http://localhost:8090
```

## Testing
The project includes multiple levels of testing.
### Unit Tests

* ```PaymentGatewayServiceTest```
* ```PaymentMapperTest```
* ```BankClientTest```
* ```CommonExceptionHandlerTest```

### Controller Tests

* ```PaymentGatewayControllerTest```

### Integration Tests

* ```PaymentGatewayIntegrationTest```

## Future Improvements

Possible improvements for production systems:
* persistent storage (database)
* retry logic for bank calls
* asynchronous processing