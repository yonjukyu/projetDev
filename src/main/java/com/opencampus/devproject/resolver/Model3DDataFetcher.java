package com.opencampus.devproject.resolver;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.opencampus.devproject.model.Model3D;
import com.opencampus.devproject.services.Model3DGenerationService;

import org.springframework.beans.factory.annotation.Autowired;

@DgsComponent
public class Model3DDataFetcher {

    private final Model3DGenerationService model3DGenerationService;

    @Autowired
    public Model3DDataFetcher(Model3DGenerationService model3DGenerationService) {
        this.model3DGenerationService = model3DGenerationService;
    }

    /**
     * Mutation GraphQL pour générer un modèle 3D
     * @param prompt Description textuelle du modèle à générer
     * @return Le modèle 3D initialisé
     */
    @DgsMutation
    public Model3D generateModel(@InputArgument String prompt) {
        return model3DGenerationService.generateModel(prompt);
    }
    
    /**
     * Requête GraphQL pour récupérer un modèle 3D par son ID
     * @param id L'ID du modèle à récupérer
     * @return Le modèle 3D correspondant
     */
    @DgsQuery
    public Model3D getModel(@InputArgument String id) {
        return model3DGenerationService.getModel(id);
    }
}