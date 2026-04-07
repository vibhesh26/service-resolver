package com.example.dependencyresolver.dto;

import java.util.ArrayList;
import java.util.List;

public class ServiceNodeRequest {
    private String serviceName;
    private List<String> dependencies = new ArrayList<>();

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }
}
