package com.example.dependencyresolver.dto;

import java.util.List;

public class DeploymentOrderResponse {
    private List<String> services;
    private int pageNumber;
    private int pageSize;
    private int totalElements;
    private int totalPages;

    public DeploymentOrderResponse(List<String> services, int pageNumber, int pageSize, int totalElements) {
        this.services = services;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (totalElements + pageSize - 1) / pageSize;
    }

    public List<String> getServices() {
        return services;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
