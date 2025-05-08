package com.opencampus.devproject.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    private static final Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);

    @Bean
    public RestTemplate restTemplate() {
        // Créer une factory qui va permettre de mettre en buffer les requêtes pour les logs
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(
                new SimpleClientHttpRequestFactory());

        // Créer un interceptor pour logger les requêtes et réponses
        ClientHttpRequestInterceptor loggingInterceptor = (request, body, execution) -> {
            logger.debug("Requête: {} {}", request.getMethod(), request.getURI());
            logger.trace("Requête body: {}", new String(body));
            
            long startTime = System.currentTimeMillis();
            try {
                // Exécuter la requête
                return execution.execute(request, body);
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                logger.debug("Requête terminée en {} ms", duration);
            }
        };

        return new RestTemplateBuilder()
                .requestFactory(() -> factory)
                .interceptors(Collections.singletonList(loggingInterceptor))
                .build();
    }
}