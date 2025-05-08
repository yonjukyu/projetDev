package com.opencampus.devproject.services;
import com.opencampus.devproject.model.Model3D;
import com.opencampus.devproject.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class Model3DGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(Model3DGenerationService.class);

    private final ShapEService shapEService;
    private final Map<String, Model3D> modelCache = new HashMap<>();

    @Autowired
    public Model3DGenerationService(ShapEService shapEService) {
        this.shapEService = shapEService;
        logger.info("Model3DGenerationService initialisé");
    }

    /**
     * Génère un modèle 3D à partir d'un prompt textuel
     * @param prompt Description textuelle du modèle 3D à générer
     * @return L'objet Model3D initialisé (le modèle sera généré de manière asynchrone)
     */
    public Model3D generateModel(String prompt) {
        logger.info("Demande de génération d'un modèle avec le prompt: '{}'", prompt);
        
        // Génération d'un ID unique
        String id = UUID.randomUUID().toString();
        logger.debug("ID généré pour le nouveau modèle: {}", id);
        
        // Création du modèle initial
        Model3D model = new Model3D();
        model.setId(id);
        model.setName("Modèle basé sur: " + prompt);
        model.setDescription("Généré à partir du prompt: " + prompt);
        model.setStatus(Status.EN_COURS);
        
        // Valeurs par défaut en attendant la génération
        model.setComplexity(3);
        model.setVertices(1000);
        model.setPolygons(2000);
        
        // Stockage dans le cache
        modelCache.put(id, model);
        logger.debug("Modèle initial créé et stocké dans le cache");
        
        // Lancement de la génération en arrière-plan
        logger.debug("Démarrage du processus de génération asynchrone");
        CompletableFuture.runAsync(() -> generateModelAsync(id, prompt))
                         .exceptionally(ex -> {
                             logger.error("Erreur non gérée dans le processus asynchrone", ex);
                             return null;
                         });
        
        return model;
    }
    
    /**
     * Traite la génération du modèle de manière asynchrone
     * @param id L'ID du modèle
     * @param prompt Le prompt de génération
     */
    private void generateModelAsync(String id, String prompt) {
        logger.info("Début du processus asynchrone de génération pour le modèle: {}", id);
        
        try {
            // Appeler le service ShapE pour générer le modèle
            Map<String, Object> result = shapEService.generateModel(prompt);
            logger.info("Modèle généré avec succès pour le prompt: '{}'", prompt);
            
            // Mise à jour du modèle avec les résultats
            Model3D model = modelCache.get(id);
            if (model != null && result != null) {
                // Extraire les statistiques du modèle
                Map<String, Object> stats = (Map<String, Object>) result.get("stats");
                int vertices = 0;
                int faces = 0;
                
                if (stats != null) {
                    if (stats.containsKey("vertices")) {
                        vertices = ((Number) stats.get("vertices")).intValue();
                    }
                    if (stats.containsKey("faces")) {
                        faces = ((Number) stats.get("faces")).intValue();
                    }
                }
                
                // Calculer la complexité
                int complexity = calculateComplexity(vertices, faces);
                
                // Mettre à jour le modèle
                model.setVertices(vertices);
                model.setPolygons(faces);
                model.setComplexity(complexity);
                model.setModelUrl((String) result.get("model_url"));
                model.setStatus(Status.COMPLETED);
                
                // Ajouter des métadonnées supplémentaires dans la description
                StringBuilder description = new StringBuilder(model.getDescription());
                description.append("\n\nFormats disponibles:");
                
                if (result.containsKey("obj_url")) {
                    description.append("\n- OBJ: ").append(result.get("obj_url"));
                }
                if (result.containsKey("ply_url")) {
                    description.append("\n- PLY: ").append(result.get("ply_url"));
                }
                if (result.containsKey("gif_url")) {
                    description.append("\n- Preview GIF: ").append(result.get("gif_url"));
                }
                
                model.setDescription(description.toString());
                
                logger.info("Modèle mis à jour avec succès: id={}, vertices={}, faces={}, complexity={}", 
                        id, vertices, faces, complexity);
            } else {
                logger.warn("Le modèle {} n'a pas été trouvé dans le cache pour la mise à jour", id);
            }
        } catch (Exception e) {
            logger.error("Erreur pendant la génération du modèle {}: {}", id, e.getMessage(), e);
            
            // En cas d'erreur, mettre à jour le statut
            Model3D model = modelCache.get(id);
            if (model != null) {
                model.setStatus(Status.FAILED);
                model.setDescription(model.getDescription() + " (Erreur: " + e.getMessage() + ")");
                logger.info("Statut du modèle {} mis à jour à FAILED", id);
            } else {
                logger.warn("Le modèle {} n'a pas été trouvé dans le cache pour la mise à jour du statut d'erreur", id);
            }
        }
    }
    
    /**
     * Calcule la complexité du modèle
     */
    private int calculateComplexity(int vertices, int faces) {
        logger.debug("Calcul de la complexité basé sur: vertices={}, faces={}", vertices, faces);
        
        if (vertices == 0 || faces == 0) {
            logger.debug("Valeurs de vertices ou faces nulles, utilisation de la complexité par défaut (3)");
            return 3; // Complexité moyenne par défaut
        }
        
        int complexity;
        if (vertices > 10000 || faces > 20000) {
            complexity = 5; // Très complexe
        } else if (vertices > 5000 || faces > 10000) {
            complexity = 4; // Complexe
        } else if (vertices > 2000 || faces > 5000) {
            complexity = 3; // Moyen
        } else if (vertices > 1000 || faces > 2000) {
            complexity = 2; // Simple
        } else {
            complexity = 1; // Très simple
        }
        
        logger.debug("Complexité calculée: {}", complexity);
        return complexity;
    }
    
    /**
     * Récupère un modèle par son ID
     */
    public Model3D getModel(String id) {
        logger.debug("Récupération du modèle avec l'ID: {}", id);
        Model3D model = modelCache.get(id);
        if (model != null) {
            logger.debug("Modèle trouvé: status={}, vertices={}, polygons={}", 
                       model.getStatus(), model.getVertices(), model.getPolygons());
        } else {
            logger.warn("Aucun modèle trouvé avec l'ID: {}", id);
        }
        return model;
    }
}