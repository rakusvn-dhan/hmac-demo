package me.dhan.hmacdemo.client;

import me.dhan.hmacdemo.model.SumRequest;
import me.dhan.hmacdemo.security.HmacUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Example client for making authenticated requests to the HMAC-protected API.
 * This class demonstrates how to generate and include HMAC signatures in requests.
 */
public class HmacApiClient {

    private static final String HMAC_HEADER_NAME = "X-HMAC-SIGNATURE";
    private static final String TIMESTAMP_HEADER_NAME = "X-TIMESTAMP";
    private final String baseUrl;
    private final String secretKey;
    private final HttpClient httpClient;

    /**
     * Creates a new HMAC API client.
     *
     * @param baseUrl   The base URL of the API (e.g., http://localhost:8080)
     * @param secretKey The secret key for generating HMAC signatures
     */
    public HmacApiClient(String baseUrl, String secretKey) {
        this.baseUrl = baseUrl;
        this.secretKey = secretKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Makes a POST request to the sum endpoint.
     *
     * @param a First number
     * @param b Second number
     * @return The sum of a and b
     * @throws IOException          If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     */
    public int sum(int a, int b) throws IOException, InterruptedException {
        String uri = "/api/demo/sum";
        String fullUrl = baseUrl + uri;

        // Create request object
        SumRequest sumRequest = new SumRequest(a, b);

        // Convert to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(sumRequest);

        // For POST requests with JSON body, the query string is empty
        String queryString = "";

        // Generate timestamp (current time in milliseconds)
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Generate HMAC signature with timestamp
        String hmacSignature = HmacUtils.generateHmacSignature("POST", uri, queryString, timestamp, secretKey);

        // Build and send request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header(HMAC_HEADER_NAME, hmacSignature)
                .header(TIMESTAMP_HEADER_NAME, timestamp)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Generate and print equivalent curl command
        String curlCommand = generateCurlCommand("POST", fullUrl, requestBody, hmacSignature, timestamp);
        var message = """
                
                Equivalent curl command for the POST request:
                %s
                
                """.formatted(curlCommand);
        System.out.println(message);

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API request failed with status code: " + response.statusCode());
        }

        return Integer.parseInt(response.body());
    }

    /**
     * Generates a curl command equivalent to the HTTP request being made.
     *
     * @param method         HTTP method (GET, POST, etc.)
     * @param url            Full URL of the request
     * @param body           Request body (for POST, PUT, etc.)
     * @param hmacSignature  HMAC signature for authentication
     * @param timestamp      Request timestamp
     * @return A string representing the equivalent curl command
     */
    private String generateCurlCommand(String method, String url, String body, String hmacSignature, String timestamp) {
        StringBuilder curlCommand = new StringBuilder();
        curlCommand.append("curl -X ").append(method).append(" \\\n");
        curlCommand.append("  \"").append(url).append("\" \\\n");
        curlCommand.append("  -H \"").append(HMAC_HEADER_NAME).append(": ").append(hmacSignature).append("\" \\\n");
        curlCommand.append("  -H \"").append(TIMESTAMP_HEADER_NAME).append(": ").append(timestamp).append("\" \\\n");
        curlCommand.append("  -H \"Content-Type: application/json\" \\\n");

        if (body != null && !body.isEmpty()) {
            // Escape double quotes in the body for proper shell escaping
            String escapedBody = body.replace("\"", "\\\"");
            curlCommand.append("  -d \"").append(escapedBody).append("\"");
        }

        return curlCommand.toString();
    }

    /**
     * Example usage of the HMAC API client.
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        try {
            // Create client with base URL and secret key
            HmacApiClient client = new HmacApiClient("http://localhost:8080", "YourSecretKeyHere123!");

            // Make authenticated request
            int result = client.sum(5, 3);
            System.out.println("Sum result: " + result);
        } catch (Exception e) {
            System.err.println("Error calling API: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
