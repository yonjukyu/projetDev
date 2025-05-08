package com.opencampus.devproject.services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class MeshyService {

    private static final Logger logger = LoggerFactory.getLogger(MeshyService.class);

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiBaseUrl;

    public MeshyService(
            @Value("${meshy.api-key}") String apiKey,
            @Value("${meshy.api-base-url}") String apiBaseUrl) {
        this.restTemplate = new RestTemplate();
        this.apiKey = apiKey;
        this.apiBaseUrl = apiBaseUrl;
        logger.info("MeshyService initialisé avec l'URL de base: {}", apiBaseUrl);
    }

    /**
     * Initie la génération d'un modèle 3D avec l'API Meshy
     * @param prompt La description textuelle du modèle 3D à générer
     * @return L'ID du job de génération
     */
    public String initiateModelGeneration(String prompt) {
        logger.debug("Démarrage de la génération de modèle avec le prompt: '{}'", prompt);
        
        // Préparation des headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        // Préparation du corps de la requête
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", prompt);
        requestBody.put("mode", "high-quality"); // Qualité élevée pour un meilleur résultat
        requestBody.put("art_style", "realistic");
        requestBody.put("should_remesh", true);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        // Appel à l'API Meshy
        String url = apiBaseUrl + "/text-to-3d";
        logger.debug("Envoi de la requête à: {}", url);
        
        try {
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            
            if (response != null && response.containsKey("id")) {
                String jobId = response.get("id").toString();
                logger.info("Job de génération créé avec l'ID: {}", jobId);
                return jobId;
            } else {
                logger.error("Réponse API invalide: {}", response);
                throw new RuntimeException("Échec de l'initialisation de la génération du modèle");
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation de la génération du modèle", e);
            throw new RuntimeException("Échec de l'initialisation de la génération du modèle: " + e.getMessage(), e);
        }
    }

    /**
     * Vérifie l'état du job de génération
     * @param jobId L'ID du job de génération
     * @return Les informations sur l'état du job
     */
    public Map<String, Object> checkJobStatus(String jobId) {
        logger.debug("Vérification du statut du job: {}", jobId);
        
        // Préparation des headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        HttpEntity<?> request = new HttpEntity<>(headers);
        
        // URL pour vérifier le statut
        String url = apiBaseUrl + "/text-to-3d/" + jobId;
        
        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                url, HttpMethod.GET, request, Map.class);
            
            Map<String, Object> response = responseEntity.getBody();
            
            if (response != null && response.containsKey("status")) {
                logger.debug("Statut du job {}: {}", jobId, response.get("status"));
            }
            
            return response;
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification du statut du job: {}", jobId, e);
            throw new RuntimeException("Échec de la vérification du statut: " + e.getMessage(), e);
        }
    }

    /**
     * Obtient l'URL de téléchargement du modèle
     * @param jobId L'ID du job de génération
     * @return L'URL de téléchargement du modèle au format GLB
     */
    public String getModelDownloadUrl(String jobId) {
        logger.debug("Récupération de l'URL de téléchargement pour le job: {}", jobId);
        
        // Récupérer d'abord les infos complètes du job
        Map<String, Object> jobInfo = checkJobStatus(jobId);
        
        if (jobInfo != null && jobInfo.containsKey("output")) {
            Map<String, Object> output = (Map<String, Object>) jobInfo.get("output");
            if (output != null && output.containsKey("glb_url")) {
                String downloadUrl = output.get("glb_url").toString();
                logger.info("URL GLB obtenue pour le job {}: {}", jobId, downloadUrl);
                return downloadUrl;
            }
        }
        
        logger.error("Impossible de trouver l'URL GLB dans la réponse: {}", jobInfo);
        throw new RuntimeException("Impossible d'obtenir l'URL de téléchargement du modèle");
    }
}