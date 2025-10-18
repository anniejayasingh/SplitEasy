package com.spliteasy.expense_sharing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("SplitEasy Expense Sharing API")
                        .description("API documentation for SplitEasy application")
                        .version("1.0.0"));
    }
}
