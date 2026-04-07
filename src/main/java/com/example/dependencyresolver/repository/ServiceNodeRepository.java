package com.example.dependencyresolver.repository;

import com.example.dependencyresolver.model.ServiceNode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ServiceNodeRepository extends JpaRepository<ServiceNode, Long> {
    Optional<ServiceNode> findByName(String name);
}
