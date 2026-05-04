package com.example.ailabb1;

import com.example.ailabb1.service.OpenRouterClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class AiLabb1ApplicationTests {

    @MockitoBean
    private OpenRouterClient openRouterClient;

    @Test
    void contextLoads() {
    }

}
