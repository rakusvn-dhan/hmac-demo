package me.dhan.hmacdemo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HMAC Demo API")
                        .description("API documentation for HMAC Demo application")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Duong Hong An")
                                .email("dhan@rakus.com.vn")));
    }
}