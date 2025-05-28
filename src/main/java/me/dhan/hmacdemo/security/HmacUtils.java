package me.dhan.hmacdemo.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for generating HMAC signatures for API requests.
 * This class can be used by API clients to generate valid signatures.
 */
public class HmacUtils {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * Generates an HMAC signature for an API request.
     *
     * @param method      HTTP method (GET, POST, etc.)
     * @param uri         Request URI (e.g., /api/demo/sum)
     * @param queryString Query string (e.g., a=5&b=3)
     * @param secretKey   The secret key used for signing
     * @return Base64 encoded HMAC signature
     * @throws RuntimeException if there's an error generating the signature
     */
    @SuppressWarnings("unused")
    public static String generateHmacSignature(String method, String uri, String queryString, String secretKey) {
        return generateHmacSignature(method, uri, queryString, null, secretKey);
    }

    /**
     * Generates an HMAC signature for an API request with timestamp.
     *
     * @param method      HTTP method (GET, POST, etc.)
     * @param uri         Request URI (e.g., /api/demo/sum)
     * @param queryString Query string (e.g., a=5&b=3)
     * @param timestamp   Request timestamp in milliseconds since epoch
     * @param secretKey   The secret key used for signing
     * @return Base64 encoded HMAC signature
     * @throws RuntimeException if there's an error generating the signature
     */
    public static String generateHmacSignature(String method, String uri, String queryString, String timestamp, String secretKey) {
        return generateHmacSignature(method, uri, queryString, timestamp, null, secretKey);
    }

    /**
     * Generates an HMAC signature for an API request with timestamp and request body.
     *
     * @param method      HTTP method (GET, POST, etc.)
     * @param uri         Request URI (e.g., /api/demo/sum)
     * @param queryString Query string (e.g., a=5&b=3)
     * @param timestamp   Request timestamp in milliseconds since epoch
     * @param requestBody JSON payload or request body
     * @param secretKey   The secret key used for signing
     * @return Base64 encoded HMAC signature
     * @throws RuntimeException if there's an error generating the signature
     */
    public static String generateHmacSignature(String method, String uri, String queryString, String timestamp, String requestBody, String secretKey) {
        try {
            StringBuilder dataToSign = new StringBuilder();
            dataToSign.append(method).append("\n");
            dataToSign.append(uri).append("\n");

            if (queryString != null && !queryString.isEmpty()) {
                dataToSign.append(queryString).append("\n");
            } else {
                dataToSign.append("\n");
            }

            if (requestBody != null && !requestBody.isEmpty()) {
                dataToSign.append(requestBody).append("\n");
            }

            if (timestamp != null && !timestamp.isEmpty()) {
                dataToSign.append(timestamp);
            }

            Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            hmac.init(secretKeySpec);
            byte[] hmacBytes = hmac.doFinal(dataToSign.toString().getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating HMAC signature", e);
        }
    }
}
