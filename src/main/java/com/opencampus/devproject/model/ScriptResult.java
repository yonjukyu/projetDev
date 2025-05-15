package com.opencampus.devproject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScriptResult {
    private String output;
    private String error;
    private int exitCode;
    private String generatedFilePath;
}