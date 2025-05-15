package com.opencampus.devproject.service;

import com.opencampus.devproject.model.ScriptResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PythonScriptService {

    private final String scriptRunnerPath;
    private final String condaEnvironment;
    private final String scriptPath;

    @Autowired
    public PythonScriptService(String pythonScriptRunner, String condaEnvironment, String scriptPath) {
        this.scriptRunnerPath = pythonScriptRunner;
        this.condaEnvironment = condaEnvironment;
        this.scriptPath = scriptPath;
    }

    public ScriptResult executeScript(String text) {
        ScriptResult result = new ScriptResult();

        String condaPath = "C:\\Users\\hugot\\miniconda3\\Scripts\\conda.exe";

        File scriptFile = new File(scriptPath);
        File scriptDir = scriptFile.getParentFile();
        // Obtenir le chemin du projet
        String projectDir = System.getProperty("user.dir");

        // Chemin vers le fichier de configuration
        String configPath = Paths.get(projectDir, "scriptPyhton", "3dtopia", "configs", "default.yaml").toString();

        List<String> command = new ArrayList<>();
        command.add("cmd.exe");
        command.add("/c");
        command.add(condaPath);
        command.add("run");
        command.add("-n");
        command.add(condaEnvironment);
        command.add("python");
        command.add("-u");
        command.add(scriptPath);
        command.add("--text");
        command.add(text);
        command.add("--no_video");
        command.add("--config");
        command.add(configPath);  // Ajout du chemin du fichier de configuration

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        // Définir le répertoire de travail au répertoire contenant le script Python
        processBuilder.directory(scriptDir);
        
        try {
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            boolean completed = process.waitFor(5, TimeUnit.MINUTES);
            
            if (!completed) {
                process.destroyForcibly();
                result.setError("Script execution timed out after 5 minutes");
                result.setExitCode(-1);
            } else {
                result.setExitCode(process.exitValue());
                result.setOutput(output.toString());
                
                // Extraire le chemin du fichier généré si présent dans la sortie
                // Cette partie peut être adaptée selon le format de sortie de votre script
                String outputStr = output.toString();
                if (outputStr.contains("Generated file:")) {
                    String[] lines = outputStr.split("\n");
                    for (String line : lines) {
                        if (line.contains("Generated file:")) {
                            result.setGeneratedFilePath(line.substring(line.indexOf(":") + 1).trim());
                            break;
                        }
                    }
                }
            }
            
        } catch (IOException | InterruptedException e) {
            result.setError("Error executing script: " + e.getMessage());
            result.setExitCode(-1);
        }
        
        return result;
    }
}