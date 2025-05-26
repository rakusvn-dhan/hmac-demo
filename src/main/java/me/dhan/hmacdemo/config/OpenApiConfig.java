package me.dhan.hmacdemo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String HMAC_SECURITY_SCHEME = "hmacAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HMAC Demo API")
                        .description("API documentation for HMAC Demo application. All requests must include an HMAC signature in the X-HMAC-SIGNATURE header.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Duong Hong An")
                                .email("dhan@rakus.com.vn")))
                .components(new Components()
                        .addSecuritySchemes(HMAC_SECURITY_SCHEME, 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-HMAC-SIGNATURE")
                                        .description("HMAC signature calculated using HmacSHA256 algorithm")))
                .addSecurityItem(new SecurityRequirement().addList(HMAC_SECURITY_SCHEME));
    }
}
