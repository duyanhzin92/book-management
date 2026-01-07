package com.example.book.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình cho tài liệu Swagger/OpenAPI
 */
@Configuration
public class SwaggerConfig {

    /**
     * Cấu hình tài liệu OpenAPI
     *
     * @return cấu hình OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Book Management API")
                        .version("1.0.0")
                        .description("RESTful API for managing books")
                        .contact(new Contact()
                                .name("Book Management Team")
                                .email("duyanhz0902@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}





