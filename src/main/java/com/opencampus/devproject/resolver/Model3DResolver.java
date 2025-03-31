package com.opencampus.devproject.resolver;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.opencampus.devproject.model.Model3D;
import com.opencampus.devproject.model.Status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@DgsComponent
public class Model3DResolver {
    
    // Une simple map pour stocker les modèles (simulant une base de données)
    private final Map<String, Model3D> models = new HashMap<>();
    
    @DgsQuery
    public Model3D getModel(@InputArgument String id) {
        return models.get(id);
    }
    
    @DgsQuery
    public List<Model3D> getAllModels() {
        return new ArrayList<>(models.values());
    }
    
    @DgsMutation
    public Model3D generateModel(@InputArgument String prompt) {
        // Génération d'un ID unique
        String id = UUID.randomUUID().toString();
        
        // Pour l'instant, nous retournons simplement un modèle de base
        // Plus tard, ici vous appellerez votre service d'IA pour générer un modèle 3D
        Model3D model = new Model3D();
        model.setId(id);
        model.setName("Modèle basé sur: " + prompt);
        model.setDescription("Généré à partir du prompt: " + prompt);
        model.setComplexity(3);
        model.setVertices(1000);
        model.setPolygons(2000);
        model.setStatus(Status.COMPLETED); // Pour l'instant, on considère que c'est instantané
        model.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        model.setModelUrl("https://example.com/models/" + id + ".glb"); // URL fictive
        
        // Stockage du modèle
        models.put(id, model);
        
        return model;
    }
}