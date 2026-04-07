# Service Resolver

A Spring Boot application for storing services and dependencies, detecting circular dependencies, and visualizing dependency graphs.

## Features

- Create services with dependencies via REST API
- Detect circular dependency before persisting
- Query impacted services for a given service
- Query deployment order with pagination
- Static frontend UI for service creation and dependency graph visualization

## Run

```bash
./mvnw spring-boot:run
```

If you don't have the Maven wrapper installed, use:

```bash
mvn spring-boot:run
```

## Default app URLs

- UI: `http://localhost:8080/service-resolver/`
- Create service: `POST http://localhost:8080/service-resolver/api/services`
- Impacted services: `GET http://localhost:8080/service-resolver/api/services/impacted-services?service=<service>`
- Deployment order: `GET http://localhost:8080/service-resolver/api/services/deployment-order?page=0&size=10`
- Full service graph: `GET http://localhost:8080/service-resolver/api/services/all`

## Example cURL

Create service:

```bash
curl -X POST http://localhost:8080/service-resolver/api/services \
  -H "Content-Type: application/json" \
  -d '{"serviceName":"config-service","dependencies":["database-service","auth-service"]}'
```

Get impacted services:

```bash
curl "http://localhost:8080/service-resolver/api/services/impacted-services?service=config-service"
```

Get deployment order:

```bash
curl "http://localhost:8080/service-resolver/api/services/deployment-order?page=0&size=10"
```

## Notes

- Spring Boot context path is configured as `/service-resolver`
- Flyway manages schema creation
- PostgreSQL is configured by default in `src/main/resources/application.properties`
