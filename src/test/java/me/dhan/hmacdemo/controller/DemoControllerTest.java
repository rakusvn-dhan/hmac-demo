package me.dhan.hmacdemo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testSum() throws Exception {
        // Test positive numbers
        mockMvc.perform(get("/api/calculator/sum")
                .param("a", "5")
                .param("b", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string("8"));

        // Test negative numbers
        mockMvc.perform(get("/api/calculator/sum")
                .param("a", "-2")
                .param("b", "7"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        // Test zero
        mockMvc.perform(get("/api/calculator/sum")
                .param("a", "0")
                .param("b", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }
}