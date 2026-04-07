package com.example.dependencyresolver.controller;

import com.example.dependencyresolver.dto.ServiceNodeRequest;
import com.example.dependencyresolver.dto.ServiceNodeResponse;
import com.example.dependencyresolver.dto.DeploymentOrderResponse;
import com.example.dependencyresolver.dto.ImpactedServicesResponse;
import com.example.dependencyresolver.exception.CircularDependencyException;
import com.example.dependencyresolver.model.ServiceNode;
import com.example.dependencyresolver.service.ServiceDependencyResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
public class ServiceNodeController {

    private final ServiceDependencyResolver resolver;

    public ServiceNodeController(ServiceDependencyResolver resolver) {
        this.resolver = resolver;
    }

    @PostMapping
    public ResponseEntity<ServiceNodeResponse> createService(@RequestBody ServiceNodeRequest request) {
        ServiceNode serviceNode = resolver.saveService(request.getServiceName(), request.getDependencies());
        ServiceNodeResponse response = new ServiceNodeResponse(
                serviceNode.getName(),
                serviceNode.getDependencies().stream()
                        .map(ServiceNode::getName)
                        .collect(Collectors.toList())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/impacted-services")
    public ResponseEntity<ImpactedServicesResponse> getImpactedServices(@RequestParam String service) {
        ImpactedServicesResponse response = new ImpactedServicesResponse(
                service,
                resolver.getImpactedServices(service)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/deployment-order")
    public ResponseEntity<DeploymentOrderResponse> getDeploymentOrder(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<String> allServices = resolver.getDeploymentOrder();
        int totalElements = allServices.size();

        int startIdx = Math.max(0, page * size);
        int endIdx = Math.min(startIdx + size, totalElements);

        List<String> pageContent = allServices.subList(startIdx, endIdx);
        DeploymentOrderResponse response = new DeploymentOrderResponse(
                pageContent,
                page,
                size,
                totalElements
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, List<String>>> getAllServices() {
        return ResponseEntity.ok(resolver.getAllServices());
    }

    @ExceptionHandler(CircularDependencyException.class)
    public ResponseEntity<String> handleCircularDependency(CircularDependencyException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}
