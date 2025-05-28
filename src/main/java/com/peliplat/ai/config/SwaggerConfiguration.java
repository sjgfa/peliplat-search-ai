package com.peliplat.ai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring AI Alibaba Tool Calling API")
                        .version("1.0.0")
                        .description("电影搜索和其他工具调用API")
                        .contact(new Contact()
                                .name("Alibaba Cloud AI")
                                .url("https://github.com/alibaba/spring-ai-alibaba")));
    }
}