CREATE TABLE service_nodes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE service_dependencies (
    service_id BIGINT NOT NULL,
    dependency_id BIGINT NOT NULL,
    CONSTRAINT fk_service FOREIGN KEY (service_id) REFERENCES service_nodes(id) ON DELETE CASCADE,
    CONSTRAINT fk_dependency FOREIGN KEY (dependency_id) REFERENCES service_nodes(id) ON DELETE CASCADE,
    CONSTRAINT pk_service_dependency PRIMARY KEY (service_id, dependency_id)
);
