package com.opencampus.devproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${shape.output.public.path:public/models}")
    private String publicModelsDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configuration pour exposer le répertoire de modèles publics
        Path modelsPath = Paths.get(publicModelsDir).toAbsolutePath().normalize();
        
        registry.addResourceHandler("/models/**")
                .addResourceLocations("file:" + modelsPath.toString() + "/")
                .setCachePeriod(3600); // Cache d'une heure
    }
}