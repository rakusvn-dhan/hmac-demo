# HMAC Demo API

Project này minh họa cách triển khai xác thực HMAC (Hash-based Message Authentication Code) để bảo mật các đầu cuối API.

## Tính năng

- API REST Spring Boot đơn giản với demo đầu cuối
- Filter xác thực HMAC bảo mật tất cả các yêu cầu API
- Tài liệu Swagger/OpenAPI
- Client mẫu để thực hiện các yêu cầu đã xác thực

## Xác thực HMAC

Tất cả các yêu cầu API phải bao gồm chữ ký HMAC trong header `X-HMAC-SIGNATURE`. Chữ ký được tính toán bằng thuật toán HmacSHA256 với dữ liệu sau:

```
<HTTP_METHOD>\n
<REQUEST_URI>\n
<QUERY_STRING>
```

Ví dụ, đối với yêu cầu GET đến `/api/demo/sum?a=5&b=3`, dữ liệu cần ký sẽ là:

```
GET
/api/demo/sum
a=5&b=3
```

## Cấu hình

Khóa bí mật HMAC được cấu hình trong `application.properties`:

```properties
hmac.secret=YourSecretKeyHere123!
```

## Ví dụ sử dụng

### Sử dụng HmacApiClient

```java
// Tạo client với URL cơ sở và khóa bí mật
HmacApiClient client = new HmacApiClient("http://localhost:8080", "YourSecretKeyHere123!");

// Thực hiện yêu cầu đã xác thực
int result = client.sum(5, 3);
System.out.println("Kết quả tổng: " + result);
```

### Tạo HMAC thủ công

```java
// Tạo chữ ký HMAC
String hmacSignature = HmacUtils.generateHmacSignature(
    "GET", 
    "/api/demo/sum", 
    "a=5&b=3", 
    "YourSecretKeyHere123!"
);

// Thêm chữ ký vào header của yêu cầu HTTP
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/api/demo/sum?a=5&b=3"))
    .header("X-HMAC-SIGNATURE", hmacSignature)
    .GET()
    .build();
```

## Tài liệu API

Tài liệu API có sẵn tại:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## Cân nhắc về bảo mật

- Giữ khóa bí mật HMAC an toàn
- Sử dụng HTTPS trong môi trường sản xuất để bảo vệ chữ ký HMAC trong quá trình truyền tải
- Cân nhắc thêm xác thực dấu thời gian để ngăn chặn các cuộc tấn công phát lại
- Trong tình huống thực tế, mỗi client nên có khóa bí mật riêng
