package com.example.dependencyresolver.service;

import com.example.dependencyresolver.exception.CircularDependencyException;
import com.example.dependencyresolver.model.ServiceNode;
import com.example.dependencyresolver.repository.ServiceNodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServiceDependencyResolver {

    private final ServiceNodeRepository repository;

    public ServiceDependencyResolver(ServiceNodeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ServiceNode saveService(String serviceName, List<String> dependencyNames) {
        Map<String, Set<String>> graph = buildGraphFromDatabase();

        Set<String> normalizedDependencies = dependencyNames == null ? Collections.emptySet()
                : dependencyNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (normalizedDependencies.contains(serviceName)) {
            throw new CircularDependencyException("Service cannot depend on itself: " + serviceName);
        }

        graph.put(serviceName, new LinkedHashSet<>(normalizedDependencies));
        normalizedDependencies.forEach(dep -> graph.putIfAbsent(dep, new LinkedHashSet<>()));

        validateNoCircularDependency(graph);

        ServiceNode serviceNode = repository.findByName(serviceName)
                .orElseGet(() -> new ServiceNode(serviceName));

        Set<ServiceNode> dependencyNodes = normalizedDependencies.stream()
                .map(name -> repository.findByName(name).orElseGet(() -> new ServiceNode(name)))
                .collect(Collectors.toSet());

        serviceNode.setDependencies(dependencyNodes);
        return repository.save(serviceNode);
    }

    private Map<String, Set<String>> buildGraphFromDatabase() {
        Map<String, Set<String>> graph = new HashMap<>();
        repository.findAll().forEach(node -> {
            Set<String> deps = node.getDependencies().stream()
                    .map(ServiceNode::getName)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            graph.put(node.getName(), deps);
        });
        return graph;
    }

    private void validateNoCircularDependency(Map<String, Set<String>> graph) {
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        for (String node : graph.keySet()) {
            if (!visited.contains(node)) {
                dfs(node, graph, visited, visiting);
            }
        }
    }

    private void dfs(String node, Map<String, Set<String>> graph, Set<String> visited, Set<String> visiting) {
        if (visiting.contains(node)) {
            throw new CircularDependencyException("Circular dependency detected involving service: " + node);
        }
        if (visited.contains(node)) {
            return;
        }

        visiting.add(node);
        for (String dependency : graph.getOrDefault(node, Collections.emptySet())) {
            dfs(dependency, graph, visited, visiting);
        }
        visiting.remove(node);
        visited.add(node);
    }

    @Transactional(readOnly = true)
    public Set<String> getImpactedServices(String serviceName) {
        ServiceNode serviceNode = repository.findByName(serviceName)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceName));

        Set<String> impacted = new HashSet<>();
        repository.findAll().forEach(node -> {
            if (node.getDependencies().contains(serviceNode)) {
                impacted.add(node.getName());
            }
        });
        return impacted;
    }

    @Transactional(readOnly = true)
    public List<String> getDeploymentOrder() {
        Map<String, Set<String>> graph = buildGraphFromDatabase();
        List<String> sortedOrder = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (String service : graph.keySet()) {
            if (!visited.contains(service)) {
                topologicalSort(service, graph, visited, sortedOrder);
            }
        }

        return sortedOrder;
    }

    @Transactional(readOnly = true)
    public Map<String, List<String>> getAllServices() {
        return buildGraphFromDatabase().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new ArrayList<>(entry.getValue())
                ));
    }

    private void topologicalSort(String node, Map<String, Set<String>> graph, Set<String> visited, List<String> result) {
        visited.add(node);

        for (String dependency : graph.getOrDefault(node, Collections.emptySet())) {
            if (!visited.contains(dependency)) {
                topologicalSort(dependency, graph, visited, result);
            }
        }

        result.add(node);
    }
}