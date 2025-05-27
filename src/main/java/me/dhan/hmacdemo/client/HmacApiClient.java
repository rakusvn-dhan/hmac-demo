package me.dhan.hmacdemo.client;

import me.dhan.hmacdemo.security.HmacUtils;

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
     * Makes a GET request to the sum endpoint.
     *
     * @param a First number
     * @param b Second number
     * @return The sum of a and b
     * @throws IOException          If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     */
    public int sum(int a, int b) throws IOException, InterruptedException {
        String uri = "/api/demo/sum";
        String queryString = "a=" + a + "&b=" + b;
        String fullUrl = baseUrl + uri + "?" + queryString;

        // Generate timestamp (current time in milliseconds)
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Generate HMAC signature with timestamp
        String hmacSignature = HmacUtils.generateHmacSignature("GET", uri, queryString, timestamp, secretKey);

        // Build and send request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header(HMAC_HEADER_NAME, hmacSignature)
                .header(TIMESTAMP_HEADER_NAME, timestamp)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API request failed with status code: " + response.statusCode());
        }

        return Integer.parseInt(response.body());
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
