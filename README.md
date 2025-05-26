# HMAC Demo API

This project demonstrates how to implement HMAC (Hash-based Message Authentication Code) validation for securing API endpoints.

## Features

- Simple Spring Boot REST API with a demo endpoint
- HMAC validation filter that secures all API requests
- Swagger/OpenAPI documentation
- Example client for making authenticated requests

## HMAC Authentication

All API requests must include an HMAC signature in the `X-HMAC-SIGNATURE` header. The signature is calculated using the HmacSHA256 algorithm with the following data:

```
<HTTP_METHOD>\n
<REQUEST_URI>\n
<QUERY_STRING>
```

For example, for a GET request to `/api/demo/sum?a=5&b=3`, the data to sign would be:

```
GET
/api/demo/sum
a=5&b=3
```

## Configuration

The HMAC secret key is configured in `application.properties`:

```properties
hmac.secret=YourSecretKeyHere123!
```

## Example Usage

### Using the HmacApiClient

```java
// Create client with base URL and secret key
HmacApiClient client = new HmacApiClient("http://localhost:8080", "YourSecretKeyHere123!");

// Make authenticated request
int result = client.sum(5, 3);
System.out.println("Sum result: " + result);
```

### Manual HMAC Generation

```java
// Generate HMAC signature
String hmacSignature = HmacUtils.generateHmacSignature(
    "GET", 
    "/api/demo/sum", 
    "a=5&b=3", 
    "YourSecretKeyHere123!"
);

// Add the signature to your HTTP request header
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/api/demo/sum?a=5&b=3"))
    .header("X-HMAC-SIGNATURE", hmacSignature)
    .GET()
    .build();
```

## API Documentation

API documentation is available at:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## Security Considerations

- Keep the HMAC secret key secure
- Use HTTPS in production to protect the HMAC signature in transit
- Consider adding timestamp validation to prevent replay attacks
- In a real-world scenario, each client should have their own secret key