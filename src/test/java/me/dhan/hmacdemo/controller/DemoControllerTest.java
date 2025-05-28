package me.dhan.hmacdemo.controller;

import me.dhan.hmacdemo.security.HmacUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.dhan.hmacdemo.model.SumRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SameParameterValue")
@SpringBootTest
@AutoConfigureMockMvc
public class DemoControllerTest {

    private static final String HMAC_HEADER_NAME = "X-HMAC-SIGNATURE";
    private static final String TIMESTAMP_HEADER_NAME = "X-TIMESTAMP";

    @Autowired
    private MockMvc mockMvc;

    @Value("${hmac.secret}")
    private String hmacSecret;

    @Test
    public void testSum() throws Exception {
        var uri = "/api/demo/sum";
        var method = "GET";
        var timestamp = String.valueOf(System.currentTimeMillis());

        // Test positive numbers
        mockMvc.perform(get(uri)
                .param("a", "5")
                .param("b", "3")
                .header(TIMESTAMP_HEADER_NAME, timestamp)
                .header(HMAC_HEADER_NAME, calculateHmac(method, uri, "a=5&b=3", timestamp)))
                .andExpect(status().isOk())
                .andExpect(content().string("8"));

        // Generate a new timestamp for each test to ensure it's current
        timestamp = String.valueOf(System.currentTimeMillis());

        // Test negative numbers
        mockMvc.perform(get(uri)
                .param("a", "-2")
                .param("b", "7")
                .header(TIMESTAMP_HEADER_NAME, timestamp)
                .header(HMAC_HEADER_NAME, calculateHmac(method, uri, "a=-2&b=7", timestamp)))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        // Generate a new timestamp for each test to ensure it's current
        timestamp = String.valueOf(System.currentTimeMillis());

        // Test zero
        mockMvc.perform(get(uri)
                .param("a", "0")
                .param("b", "0")
                .header(TIMESTAMP_HEADER_NAME, timestamp)
                .header(HMAC_HEADER_NAME, calculateHmac(method, uri, "a=0&b=0", timestamp)))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    public void testSumWithoutHmacHeader() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        mockMvc.perform(get("/api/demo/sum")
                .param("a", "5")
                .param("b", "3")
                .header(TIMESTAMP_HEADER_NAME, timestamp))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSumWithInvalidHmacHeader() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        mockMvc.perform(get("/api/demo/sum")
                .param("a", "5")
                .param("b", "3")
                .header(TIMESTAMP_HEADER_NAME, timestamp)
                .header(HMAC_HEADER_NAME, "invalidSignature"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSumWithoutTimestampHeader() throws Exception {
        mockMvc.perform(get("/api/demo/sum")
                .param("a", "5")
                .param("b", "3")
                .header(HMAC_HEADER_NAME, calculateHmac("GET", "/api/demo/sum", "a=5&b=3")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSumWithExpiredTimestamp() throws Exception {
        // Create a timestamp from 10 minutes ago (beyond the 5-minute validity window)
        String expiredTimestamp = String.valueOf(System.currentTimeMillis() - 10 * 60 * 1000);

        mockMvc.perform(get("/api/demo/sum")
                .param("a", "5")
                .param("b", "3")
                .header(TIMESTAMP_HEADER_NAME, expiredTimestamp)
                .header(HMAC_HEADER_NAME, calculateHmac("GET", "/api/demo/sum", "a=5&b=3", expiredTimestamp)))
                .andExpect(status().isUnauthorized());
    }

    private String calculateHmac(String method, String uri, String queryString, String timestamp) {
        return calculateHmac(method, uri, queryString, timestamp, null);
    }

    private String calculateHmac(String method, String uri, String queryString, String timestamp, String requestBody) {
        return HmacUtils.generateHmacSignature(method, uri, queryString, timestamp, requestBody, hmacSecret);
    }

    private String calculateHmac(String method, String uri, String queryString) {
        // For backward compatibility with existing tests
        return calculateHmac(method, uri, queryString, String.valueOf(System.currentTimeMillis()));
    }

    @Test
    public void testSumPost() throws Exception {
        var uri = "/api/demo/sum";
        var method = "POST";
        var timestamp = String.valueOf(System.currentTimeMillis());

        // Create the request object
        SumRequest request = new SumRequest(5, 3);

        // Convert request to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(request);

        // For POST requests with JSON body, the query string is empty
        String queryString = "";

        // Test positive numbers
        mockMvc.perform(post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header(TIMESTAMP_HEADER_NAME, timestamp)
                .header(HMAC_HEADER_NAME, calculateHmac(method, uri, queryString, timestamp, requestJson)))
                .andExpect(status().isOk())
                .andExpect(content().string("8"));

        // Generate a new timestamp for each test to ensure it's current
        timestamp = String.valueOf(System.currentTimeMillis());

        // Test negative numbers
        request = new SumRequest(-2, 7);
        requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header(TIMESTAMP_HEADER_NAME, timestamp)
                .header(HMAC_HEADER_NAME, calculateHmac(method, uri, queryString, timestamp, requestJson)))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        // Generate a new timestamp for each test to ensure it's current
        timestamp = String.valueOf(System.currentTimeMillis());

        // Test zero
        request = new SumRequest(0, 0);
        requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header(TIMESTAMP_HEADER_NAME, timestamp)
                .header(HMAC_HEADER_NAME, calculateHmac(method, uri, queryString, timestamp, requestJson)))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    public void testSumPostWithoutHmacHeader() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Create the request object
        SumRequest request = new SumRequest(5, 3);

        // Convert request to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/demo/sum")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header(TIMESTAMP_HEADER_NAME, timestamp))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSumPostWithInvalidHmacHeader() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Create the request object
        SumRequest request = new SumRequest(5, 3);

        // Convert request to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/demo/sum")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header(TIMESTAMP_HEADER_NAME, timestamp)
                .header(HMAC_HEADER_NAME, "invalidSignature"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSumPostWithoutTimestampHeader() throws Exception {
        // Create the request object
        SumRequest request = new SumRequest(5, 3);

        // Convert request to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(request);

        // For POST requests with JSON body, the query string is empty
        String queryString = "";

        mockMvc.perform(post("/api/demo/sum")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header(HMAC_HEADER_NAME, calculateHmac("POST", "/api/demo/sum", queryString, String.valueOf(System.currentTimeMillis()), requestJson)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSumPostWithExpiredTimestamp() throws Exception {
        // Create a timestamp from 10 minutes ago (beyond the 5-minute validity window)
        String expiredTimestamp = String.valueOf(System.currentTimeMillis() - 10 * 60 * 1000);

        // Create the request object
        SumRequest request = new SumRequest(5, 3);

        // Convert request to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(request);

        // For POST requests with JSON body, the query string is empty
        String queryString = "";

        mockMvc.perform(post("/api/demo/sum")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header(TIMESTAMP_HEADER_NAME, expiredTimestamp)
                .header(HMAC_HEADER_NAME, calculateHmac("POST", "/api/demo/sum", queryString, expiredTimestamp, requestJson)))
                .andExpect(status().isUnauthorized());
    }
}
