package com.daehanforeigner.capstone.domain.pronunciation_attempt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FastApiConfig {

    @Bean
    public RestTemplate fastApiRestTemplate() {
        return new RestTemplate();
    }
}
