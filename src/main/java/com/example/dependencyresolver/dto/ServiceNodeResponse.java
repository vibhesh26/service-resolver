package com.example.dependencyresolver.dto;

import java.util.List;

public class ServiceNodeResponse {
    private String serviceName;
    private List<String> dependencies;

    public ServiceNodeResponse(String serviceName, List<String> dependencies) {
        this.serviceName = serviceName;
        this.dependencies = dependencies;
    }

    public String getServiceName() {
        return serviceName;
    }

    public List<String> getDependencies() {
        return dependencies;
    }
}
