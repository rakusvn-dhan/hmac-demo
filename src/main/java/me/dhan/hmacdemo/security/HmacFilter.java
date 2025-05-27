package me.dhan.hmacdemo.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class HmacFilter implements Filter {

    private static final String HMAC_HEADER_NAME = "X-HMAC-SIGNATURE";
    private static final String TIMESTAMP_HEADER_NAME = "X-TIMESTAMP";
    private static final long TIMESTAMP_VALIDITY_MINUTES = 5;

    @Value("${hmac.secret:defaultSecretKey}")
    private String hmacSecret;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip HMAC validation for Swagger UI and API docs
        String requestURI = httpRequest.getRequestURI();
        if (isSwaggerRequest(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // Wrap the request to allow reading the body multiple times
        CachedBodyHttpServletRequest cachedBodyRequest = new CachedBodyHttpServletRequest(httpRequest);

        // Validate timestamp
        String timestamp = cachedBodyRequest.getHeader(TIMESTAMP_HEADER_NAME);
        if (!StringUtils.hasText(timestamp)) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Missing timestamp header");
            return;
        }

        if (!isValidTimestamp(timestamp)) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Expired or invalid timestamp");
            return;
        }

        // Validate HMAC signature
        String hmacHeader = cachedBodyRequest.getHeader(HMAC_HEADER_NAME);
        if (!StringUtils.hasText(hmacHeader)) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Missing HMAC signature header");
            return;
        }

        String calculatedHmac = calculateHmac(cachedBodyRequest, timestamp);

        if (!hmacHeader.equals(calculatedHmac)) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Invalid HMAC signature");
            return;
        }

        chain.doFilter(cachedBodyRequest, response);
    }

    private boolean isSwaggerRequest(String requestURI) {
        return requestURI.contains("/swagger-ui") || 
               requestURI.contains("/api-docs") || 
               requestURI.equals("/") || 
               requestURI.contains("/favicon.ico");
    }

    private String calculateHmac(HttpServletRequest request, String timestamp) {
        // Get the request method and URI
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // Try to get query string first
        String queryString = request.getQueryString();

        // If query string is null (which happens in MockMvc tests), build it from parameters
        if (queryString == null && !request.getParameterMap().isEmpty()) {
            StringBuilder paramsBuilder = new StringBuilder();
            request.getParameterMap().forEach((key, values) -> {
                for (String value : values) {
                    if (!paramsBuilder.isEmpty()) {
                        paramsBuilder.append("&");
                    }
                    paramsBuilder.append(key).append("=").append(value);
                }
            });
            queryString = paramsBuilder.toString();
        }

        // Get request body if available
        String requestBody = null;
        if (request instanceof CachedBodyHttpServletRequest) {
            requestBody = ((CachedBodyHttpServletRequest) request).getBody();
        }

        // Use HmacUtils to generate the signature with timestamp and request body
        return HmacUtils.generateHmacSignature(method, uri, queryString, timestamp, requestBody, hmacSecret);
    }

    /**
     * Validates if the provided timestamp is within the allowed time window.
     * 
     * @param timestamp The timestamp to validate (milliseconds since epoch)
     * @return true if the timestamp is valid, false otherwise
     */
    private boolean isValidTimestamp(String timestamp) {
        try {
            long timestampValue = Long.parseLong(timestamp);
            Instant requestTime = Instant.ofEpochMilli(timestampValue);
            Instant now = Instant.now();

            // Check if timestamp is not in the future (with a small tolerance)
            if (requestTime.isAfter(now.plus(1, ChronoUnit.MINUTES))) {
                return false;
            }

            // Check if timestamp is not too old
            return !requestTime.isBefore(now.minus(TIMESTAMP_VALIDITY_MINUTES, ChronoUnit.MINUTES));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * A wrapper for HttpServletRequest that caches the request body so it can be read multiple times.
     * This is necessary because the request body can only be read once from the original request.
     */
    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;
        private final String cachedBodyString;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            // Read the request body and cache it
            InputStream requestInputStream = request.getInputStream();
            this.cachedBody = StreamUtils.copyToByteArray(requestInputStream);
            this.cachedBodyString = new String(cachedBody, StandardCharsets.UTF_8);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new CachedServletInputStream(cachedBody);
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }

        /**
         * Returns the cached request body as a string.
         * 
         * @return The request body as a string
         */
        public String getBody() {
            return cachedBodyString;
        }

        /**
         * A ServletInputStream implementation that reads from a cached byte array.
         */
        private static class CachedServletInputStream extends ServletInputStream {
            private final ByteArrayInputStream inputStream;

            public CachedServletInputStream(byte[] cachedBody) {
                this.inputStream = new ByteArrayInputStream(cachedBody);
            }

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public boolean isFinished() {
                return inputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException("setReadListener is not supported");
            }
        }
    }
}
