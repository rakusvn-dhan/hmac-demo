package me.dhan.hmacdemo.controller;

import me.dhan.hmacdemo.security.HmacUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DemoControllerTest {

    private static final String HMAC_HEADER_NAME = "X-HMAC-SIGNATURE";

    @Autowired
    private MockMvc mockMvc;

    @Value("${hmac.secret}")
    private String hmacSecret;

    @Test
    public void testSum() throws Exception {
        var uri = "/api/demo/sum";
        var method = "GET";
        
        // Test positive numbers
        mockMvc.perform(get(uri)
                .param("a", "5")
                .param("b", "3")
                .header(HMAC_HEADER_NAME, calculateHmac(method, uri, "a=5&b=3")))
                .andExpect(status().isOk())
                .andExpect(content().string("8"));

        // Test negative numbers
        mockMvc.perform(get(uri)
                .param("a", "-2")
                .param("b", "7")
                .header(HMAC_HEADER_NAME, calculateHmac(method, uri, "a=-2&b=7")))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        // Test zero
        mockMvc.perform(get(uri)
                .param("a", "0")
                .param("b", "0")
                .header(HMAC_HEADER_NAME, calculateHmac(method, uri, "a=0&b=0")))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    public void testSumWithoutHmacHeader() throws Exception {
        mockMvc.perform(get("/api/demo/sum")
                .param("a", "5")
                .param("b", "3"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSumWithInvalidHmacHeader() throws Exception {
        mockMvc.perform(get("/api/demo/sum")
                .param("a", "5")
                .param("b", "3")
                .header(HMAC_HEADER_NAME, "invalidSignature"))
                .andExpect(status().isUnauthorized());
    }

    private String calculateHmac(String method, String uri, String queryString) {
        return HmacUtils.generateHmacSignature(method, uri, queryString, hmacSecret);
    }
}
