package com.opencampus.devproject.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsTypeDefinitionRegistry;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.SchemaParser;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
@Configuration
public class DgsConfig {

    private final ResourceLoader resourceLoader;

    public DgsConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    @DgsTypeDefinitionRegistry
    public TypeDefinitionRegistry typeDefinitionRegistry() throws IOException {
        SchemaParser schemaParser = new SchemaParser();

        // Essayer de charger le schéma depuis différents chemins possibles
        Resource schemaResource = resourceLoader.getResource("classpath:graphql/schema.graphqls");
        if (!schemaResource.exists()) {
            schemaResource = resourceLoader.getResource("classpath:ressources/graphql/schema.graphqls");
        }
        if (!schemaResource.exists()) {
            schemaResource = resourceLoader.getResource("classpath:schema.graphqls");
        }

        try (InputStreamReader reader = new InputStreamReader(schemaResource.getInputStream(), StandardCharsets.UTF_8)) {
            return schemaParser.parse(reader);
        }
    }
}