# Equipment Management API

## What it does

REST API for managing IT equipment lifecycle and employee allocation workflows. Built with hexagonal architecture (Buckpal-style) across four Maven modules: `domain`, `application`, `infrastructure`, and `bootstrap`.

Core capabilities:

- Register equipment (computers, monitors, keyboards, mice) with condition scoring
- Track equipment state transitions: AVAILABLE, RESERVED, ASSIGNED, RETIRED
- Create allocation requests against employee policies with type, quantity, and condition constraints
- Async allocation processing via RabbitMQ using a backtracking algorithm with hard/soft constraint matching
- Confirm or cancel allocations with automatic equipment state management
- Redis caching on equipment queries, evicted on writes
- Rate limiting (Bucket4j, 100 req/min per IP), Prometheus metrics, health probes

Equipment types: `MAIN_COMPUTER`, `MONITOR`, `KEYBOARD`, `MOUSE`

## Build

```bash
mvn clean package -DskipTests
```

To build with tests (requires Docker for Testcontainers):

```bash
mvn clean verify
```

## Run

Start the full stack (PostgreSQL, Redis, RabbitMQ, application):

```bash
docker compose up --build
```

The application starts on port 8080. Services included:

| Service    | Port  |
|------------|-------|
| API        | 8080  |
| PostgreSQL | 5432  |
| Redis      | 6379  |
| RabbitMQ   | 5672  |
| RabbitMQ UI| 15672 |

Flyway runs four migrations on startup to create `equipment`, `allocation_request`, `allocation_equipment`, and `policy_item` tables.

## Test

Run all tests:

```bash
mvn clean test
```

Test layers:

- **Domain unit tests** -- `EquipmentTest`, `AllocationRequestTest`, `AllocationAlgorithmTest`, `ConditionScoreTest`, `PolicyItemTest`
- **Application unit tests** -- `EquipmentServiceTest`, `AllocationServiceTest`, `AllocationProcessorServiceTest`, command validation tests
- **Infrastructure tests** -- `EquipmentControllerWebMvcTest`, `AllocationControllerWebMvcTest`, persistence adapter tests, repository tests
- **Integration tests** -- `EquipmentLifecycleIntegrationTest`, `AllocationFlowIntegrationTest`, `SecurityHeadersIntegrationTest`, `ActuatorHealthIntegrationTest`, `ErrorHandlingIntegrationTest`
- **Architecture tests** -- `ArchitectureTest` (ArchUnit rules for hexagonal layer enforcement)

## API

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

OpenAPI spec: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Equipment endpoints

```bash
# Register equipment
curl -s -X POST http://localhost:8080/equipments \
  -H "Content-Type: application/json" \
  -d '{
    "type": "MAIN_COMPUTER",
    "brand": "Dell",
    "model": "Latitude 5540",
    "conditionScore": 0.95,
    "purchaseDate": "2025-01-15"
  }'

# List all equipment
curl -s http://localhost:8080/equipments

# List by state
curl -s "http://localhost:8080/equipments?state=AVAILABLE"

# Retire equipment
curl -s -X POST http://localhost:8080/equipments/{id}/retire \
  -H "Content-Type: application/json" \
  -d '{"reason": "End of lifecycle"}'
```

### Allocation endpoints

```bash
# Create allocation request
curl -s -X POST http://localhost:8080/allocations \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "EMP-001",
    "policyItems": [
      {
        "equipmentType": "MAIN_COMPUTER",
        "quantity": 1,
        "minimumConditionScore": 0.8,
        "preferredBrand": "Dell",
        "preferRecent": true
      }
    ]
  }'

# Get allocation details
curl -s http://localhost:8080/allocations/{id}

# Confirm allocation (assigns equipment)
curl -s -X POST http://localhost:8080/allocations/{id}/confirm

# Cancel allocation (releases equipment)
curl -s -X POST http://localhost:8080/allocations/{id}/cancel
```

### Observability endpoints

```bash
# Health check (includes database and Redis status)
curl -s http://localhost:8080/actuator/health

# Kubernetes probes
curl -s http://localhost:8080/actuator/health/liveness
curl -s http://localhost:8080/actuator/health/readiness

# Prometheus metrics
curl -s http://localhost:8080/actuator/prometheus
```

Custom business metrics: `equipment.registered`, `allocation.created`, `allocation.confirmed`, `allocation.processing.duration`.

## API Examples

All examples below were tested against a running instance started via `docker compose up --build`.

### Health Check

```bash
curl -s http://localhost:8080/actuator/health
# Response: {"status":"UP","components":{"db":{"status":"UP"},...}}
```

### Register Equipment

```bash
curl -s -X POST http://localhost:8080/equipments \
  -H "Content-Type: application/json" \
  -d '{
    "type": "MAIN_COMPUTER",
    "brand": "Apple",
    "model": "MacBook Pro 16",
    "conditionScore": 0.95,
    "purchaseDate": "2025-06-15"
  }'
# Response (HTTP 201): {"id":"<uuid>"}
```

### List All Equipment

```bash
curl -s http://localhost:8080/equipments
# Response: [{"id":"...","type":"MAIN_COMPUTER","brand":"Apple","model":"MacBook Pro 16",...}]
```

### Filter Equipment by State

```bash
curl -s "http://localhost:8080/equipments?state=AVAILABLE"
# Response: [{"id":"...","state":"AVAILABLE",...}]
```

### Retire Equipment

```bash
curl -s -X POST http://localhost:8080/equipments/{id}/retire \
  -H "Content-Type: application/json" \
  -d '{"reason": "End of life - damaged"}'
# Response: HTTP 204 (no body)
```

### Create Allocation Request

```bash
curl -s -X POST http://localhost:8080/allocations \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "emp-001",
    "policyItems": [
      {
        "equipmentType": "MAIN_COMPUTER",
        "quantity": 1,
        "minimumConditionScore": 0.8,
        "preferredBrand": "Apple",
        "preferRecent": true
      },
      {
        "equipmentType": "MONITOR",
        "quantity": 2,
        "minimumConditionScore": 0.7,
        "preferredBrand": "Dell",
        "preferRecent": false
      }
    ]
  }'
# Response (HTTP 201): {"id":"<uuid>"}
```

### Get Allocation Details

```bash
curl -s http://localhost:8080/allocations/{id}
# Response: {"id":"...","employeeId":"emp-001","status":"PENDING",...}
```

### Confirm Allocation

```bash
curl -s -X POST http://localhost:8080/allocations/{id}/confirm
# Response: HTTP 204 (no body)
```

### Cancel Allocation

```bash
curl -s -X POST http://localhost:8080/allocations/{id}/cancel
# Response: HTTP 204 (no body)
```

### Actuator Endpoints

```bash
curl -s http://localhost:8080/actuator/metrics
# Response: {"names":["equipment.registered","allocation.created",...]}

curl -s http://localhost:8080/actuator/prometheus
# Response: Prometheus-format metrics
```

### Swagger UI

Open in browser: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

OpenAPI spec: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)