package com.example.dependencyresolver.dto;

import java.util.Set;

public class ImpactedServicesResponse {
    private String serviceName;
    private Set<String> impactedServices;

    public ImpactedServicesResponse(String serviceName, Set<String> impactedServices) {
        this.serviceName = serviceName;
        this.impactedServices = impactedServices;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Set<String> getImpactedServices() {
        return impactedServices;
    }
}
