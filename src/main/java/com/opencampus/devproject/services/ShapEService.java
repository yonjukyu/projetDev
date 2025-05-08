package com.opencampus.devproject.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ShapEService {

    private static final Logger logger = LoggerFactory.getLogger(ShapEService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${shape.python.path:python}")
    private String pythonPath;
    
    @Value("${shape.script.path:src/main/resources/scripts/generate_model.py}")
    private String scriptPath;
    
    @Value("${shape.output.dir:models}")
    private String outputDir;
    
    @Value("${shape.output.public.path:public/models}")
    private String publicOutputDir;
    
    @Value("${shape.model.id:openai/shap-e}")
    private String modelId;
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;
    
    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Génère un modèle 3D à partir d'un prompt en utilisant Shap-E
     * @param prompt Description textuelle du modèle 3D
     * @return Map avec les informations sur le modèle généré
     */
    public Map<String, Object> generateModel(String prompt) throws Exception {
        logger.info("Génération d'un modèle 3D avec le prompt: '{}'", prompt);
        
        // Créer les répertoires si nécessaires
        File outputDirFile = new File(outputDir);
        if (!outputDirFile.exists()) {
            if (outputDirFile.mkdirs()) {
                logger.info("Répertoire de sortie créé: {}", outputDir);
            } else {
                logger.error("Impossible de créer le répertoire de sortie: {}", outputDir);
                throw new RuntimeException("Impossible de créer le répertoire de sortie");
            }
        }
        
        File publicOutputDirFile = new File(publicOutputDir);
        if (!publicOutputDirFile.exists()) {
            if (publicOutputDirFile.mkdirs()) {
                logger.info("Répertoire public créé: {}", publicOutputDir);
            } else {
                logger.error("Impossible de créer le répertoire public: {}", publicOutputDir);
                throw new RuntimeException("Impossible de créer le répertoire public");
            }
        }
        
        // Chaque génération a son propre dossier unique
        String jobId = UUID.randomUUID().toString();
        String jobDir = outputDir + "/" + jobId;
        File jobDirFile = new File(jobDir);
        if (!jobDirFile.mkdirs()) {
            logger.error("Impossible de créer le répertoire du job: {}", jobDir);
            throw new RuntimeException("Impossible de créer le répertoire du job");
        }
        
        // Construire la commande pour exécuter le script Python
        ProcessBuilder processBuilder = new ProcessBuilder(
                pythonPath,
                scriptPath,
                prompt,
                jobDir,
                modelId
        );
        
        processBuilder.redirectErrorStream(true);
        
        logger.debug("Exécution de la commande: {}", String.join(" ", processBuilder.command()));
        
        // Exécuter le processus
        Process process = processBuilder.start();
        
        // Lire la sortie du processus
        StringBuilder output = new StringBuilder();
        String metadataPath = null;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                
                // Capturer le chemin du fichier de métadonnées
                if (line.startsWith("OUTPUT:")) {
                    metadataPath = line.substring(7).trim();
                }
                
                // Log pour débug
                logger.debug("Python: {}", line);
            }
        }
        
        // Attendre que le processus se termine
        boolean completed = process.waitFor(15, TimeUnit.MINUTES);
        
        if (!completed) {
            process.destroyForcibly();
            logger.error("Timeout lors de la génération du modèle après 15 minutes");
            throw new RuntimeException("Timeout lors de la génération du modèle");
        }
        
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            logger.error("Erreur lors de l'exécution du script Python. Code de sortie: {}", exitCode);
            logger.error("Sortie du script: {}", output.toString());
            throw new RuntimeException("Erreur lors de l'exécution du script Python");
        }
        
        // Lire le fichier de métadonnées
        if (metadataPath == null || !new File(metadataPath).exists()) {
            logger.error("Le fichier de métadonnées n'a pas été généré");
            throw new RuntimeException("Le fichier de métadonnées n'a pas été généré");
        }
        
        logger.info("Lecture du fichier de métadonnées: {}", metadataPath);
        Map<String, Object> metadata = objectMapper.readValue(new File(metadataPath), Map.class);
        
        // Copier les fichiers vers le répertoire public
        Map<String, String> publicUrls = copyFilesToPublicDir(metadata, jobId);
        metadata.putAll(publicUrls);
        
        logger.info("Génération terminée avec succès. JobId: {}", jobId);
        
        return metadata;
    }
    
    /**
     * Copie les fichiers générés vers le répertoire public et ajoute les URLs publiques aux métadonnées
     */
    private Map<String, String> copyFilesToPublicDir(Map<String, Object> metadata, String jobId) throws Exception {
        Map<String, String> publicUrls = new HashMap<>();
        
        // Obtenir les chemins des fichiers
        String objPath = (String) metadata.get("obj_path");
        String plyPath = (String) metadata.get("ply_path");
        String gifPath = (String) metadata.get("gif_path");
        
        // Définir les noms de fichiers de destination
        String baseFilename = jobId;
        String publicObjPath = publicOutputDir + "/" + baseFilename + ".obj";
        String publicPlyPath = publicOutputDir + "/" + baseFilename + ".ply";
        String publicGifPath = publicOutputDir + "/" + baseFilename + ".gif";
        
        // Copier les fichiers
        if (objPath != null && new File(objPath).exists()) {
            Files.copy(Path.of(objPath), Path.of(publicObjPath), StandardCopyOption.REPLACE_EXISTING);
            publicUrls.put("obj_url", getPublicUrl(baseFilename + ".obj"));
        }
        
        if (plyPath != null && new File(plyPath).exists()) {
            Files.copy(Path.of(plyPath), Path.of(publicPlyPath), StandardCopyOption.REPLACE_EXISTING);
            publicUrls.put("ply_url", getPublicUrl(baseFilename + ".ply"));
        }
        
        if (gifPath != null && new File(gifPath).exists()) {
            Files.copy(Path.of(gifPath), Path.of(publicGifPath), StandardCopyOption.REPLACE_EXISTING);
            publicUrls.put("gif_url", getPublicUrl(baseFilename + ".gif"));
        }
        
        // Définir l'URL du modèle principal (pour compatibilité avec l'interface)
        if (publicUrls.containsKey("obj_url")) {
            publicUrls.put("model_url", publicUrls.get("obj_url"));
        } else if (publicUrls.containsKey("ply_url")) {
            publicUrls.put("model_url", publicUrls.get("ply_url"));
        }
        
        return publicUrls;
    }
    
    /**
     * Construit une URL publique pour accéder au fichier
     */
    private String getPublicUrl(String filename) {
        String baseUrl = "http://localhost:" + serverPort;
        String path = contextPath.isEmpty() ? "" : contextPath;
        path += path.endsWith("/") ? "" : "/";
        path += "models/" + filename;
        
        return baseUrl + path;
    }
}