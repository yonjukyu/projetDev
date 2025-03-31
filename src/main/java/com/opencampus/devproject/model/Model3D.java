package com.opencampus.devproject.model;

public class Model3D {
    private String id;
    private String name;
    private String description;
    private Integer complexity;
    private Integer vertices;
    private Integer polygons;
    private Status status;
    private String createdAt;
    private String modelUrl;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getComplexity() {
        return complexity;
    }
    
    public void setComplexity(Integer complexity) {
        this.complexity = complexity;
    }
    
    public Integer getVertices() {
        return vertices;
    }
    
    public void setVertices(Integer vertices) {
        this.vertices = vertices;
    }
    
    public Integer getPolygons() {
        return polygons;
    }
    
    public void setPolygons(Integer polygons) {
        this.polygons = polygons;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getModelUrl() {
        return modelUrl;
    }
    
    public void setModelUrl(String modelUrl) {
        this.modelUrl = modelUrl;
    }
}