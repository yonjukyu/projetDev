package com.opencampus.devproject.controller;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.opencampus.devproject.model.ScriptResult;
import com.opencampus.devproject.service.PythonScriptService;

import org.springframework.beans.factory.annotation.Autowired;

@DgsComponent
public class GraphQLController {

    private final PythonScriptService pythonScriptService;

    @Autowired
    public GraphQLController(PythonScriptService pythonScriptService) {
        this.pythonScriptService = pythonScriptService;
    }

    @DgsQuery
    public ScriptResult generate3DTopia(@InputArgument String text) {
        return pythonScriptService.executeScript(text);
    }
}