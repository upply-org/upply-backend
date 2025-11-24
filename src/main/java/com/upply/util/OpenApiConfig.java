package com.upply.util;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .info(
                        new Info()
                                .title("UPPLY")
                                .description("Bring your coffee, open Swagger… and pray backend didn’t ‘surprise’ you today.")
                                .version("0.0.1")
                );
    }
}
