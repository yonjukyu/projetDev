package com.opencampus.devproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Configuration
public class PythonConfig {

    @Bean
    public String pythonScriptRunner() throws IOException {
        // Créer un fichier temporaire pour le script Python helper si nécessaire
        Path scriptRunnerPath = Paths.get(System.getProperty("java.io.tmpdir"), "script_runner.py");

        if (!Files.exists(scriptRunnerPath)) {
            ClassPathResource resource = new ClassPathResource("python/script_runner.py");
            Files.copy(resource.getInputStream(), scriptRunnerPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return scriptRunnerPath.toString();
    }

    @Bean
    public String condaEnvironment() {
        return "3dtopia"; // Nom de l'environnement conda
    }

    @Bean
    public String scriptPath() {
        // Utiliser le chemin absolu du projet
        // Nous pouvons récupérer le chemin du projet en cours d'exécution
        try {
            // Obtenir le chemin du répertoire du projet
            String projectDir = new File(".").getCanonicalPath();
            return projectDir + "/scriptPyhton/3dtopia/sample_stage1.py";
        } catch (IOException e) {
            // En cas d'erreur, retourner le chemin direct
            return "./scriptPyhton/3dtopia/sample_stage1.py";
        }
    }
}