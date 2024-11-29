package com.secor.loxprofileservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig
{
    @Bean
    public WebClient webClient_1(WebClient.Builder webClientBuilder)
    {
        return webClientBuilder
                .baseUrl("http://localhost:8081/api/v1/validate")
                .filter(new LoggingWebClientFilter())
                .build();
    }
}
