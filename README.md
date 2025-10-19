# KAIBURR_TASK1
# Task Runner API (Backend)

Spring Boot 3 (Java 17) REST API to create tasks and run them inside short‑lived Kubernetes pods, persisting results in MongoDB. Uses the Fabric8 Kubernetes client to create pods and capture their logs.

## Features
- CRUD for tasks with MongoDB persistence.
- Execute a task command in a Kubernetes pod; return logs as output.
- Command validation to block dangerous tokens.
- CORS configuration for the frontend.
- Minimal actuator endpoints for health/info.

## Project Modules
- Controller: `TaskController` (`/api/**`)
- Service: `TaskService`, `KubernetesJobRunner`, `CommandValidator`
- Model: `Task`, `TaskExecution`
- Repository: `TaskRepository`
- Config: `CorsConfig`

## Requirements
- Java 17 and Maven
- MongoDB (local or in cluster)
- Docker (for container build/run)
- Kubernetes cluster (e.g., Minikube) + `kubectl`

## Configuration
Values can be set via environment variables (defaults shown from `application.yml`).
- `server.port` = `8081`
- `SPRING_DATA_MONGODB_URI` (default: `mongodb://localhost:27017/taskrunner`)
- `RUNNER_NAMESPACE` (default: `task-runner`)
- `RUNNER_JOB_TIMEOUT_SECONDS` (default: `60`)
- `RUNNER_IMAGE` (default: `busybox:1.36.1`)
- `FRONTEND_ORIGIN` (default: `http://localhost:5173`)

## Build & Run Locally
```bash
# From backend/
mvn -DskipTests package
java -jar target/task-runner-api-0.0.1-SNAPSHOT.jar
# App listens on http://localhost:8081
```

### Local Test (without Kubernetes pod execution)
```bash
# List tasks
curl -s http://localhost:8081/api/tasks | python3 -m json.tool

# Create/update a task
curl -i -X PUT http://localhost:8081/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{"id":"123","name":"Hello","owner":"You","command":"echo hello"}'

# Trigger execution (requires Kubernetes access as configured)
curl -i -X PUT http://localhost:8081/api/tasks/123/executions
```

## Docker
```bash
# Build jar then image (from backend/)
mvn -DskipTests package
docker build -t task-runner-api:podrunner .

# Run container pointing to a local MongoDB on the host
docker run --rm -p 8081:8081 \
  -e SPRING_DATA_MONGODB_URI="mongodb://host.docker.internal:27017/taskrunner" \
  task-runner-api:podrunner
```


