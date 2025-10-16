# KAIBURR_TASK1
# Task Runner API (Backend)

Spring Boot 3 (Java 17) REST API to create tasks and run them inside short‑lived Kubernetes pods, persisting results in MongoDB. Uses the Fabric8 Kubernetes client to create pods and capture their logs.

## Features
- CRUD for tasks with MongoDB persistence.
- Execute a task command in a Kubernetes pod; return logs as output.
- Command validation to block dangerous tokens.
- CORS configuration for the frontend.
- Minimal actuator endpoints for health/info.

## Tech Stack
- Java 17, Spring Boot 3
- MongoDB (Spring Data MongoDB)
- Fabric8 Kubernetes Client
- Maven, Docker

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
# (host.docker.internal works on Docker Desktop; adjust for your platform)
docker run --rm -p 8081:8081 \
  -e SPRING_DATA_MONGODB_URI="mongodb://host.docker.internal:27017/taskrunner" \
  task-runner-api:podrunner
```

## Kubernetes (Minikube)
```bash
# Start cluster and use its Docker daemon (run these in your shell)
minikube start
eval $(minikube docker-env)

# Build inside Minikube's Docker (from backend/)
mvn -DskipTests package
docker build -t task-runner-api:podrunner .

# Apply manifests (from backend/, use paths relative to repo root)
kubectl apply -f ../k8s/namespace.yaml
kubectl apply -f ../k8s/rbac.yaml
kubectl apply -f ../k8s/mongo-statefulset.yaml
kubectl apply -f ../k8s/app-deployment.yaml
kubectl apply -f ../k8s/app-service-nodeport.yaml

# Verify resources
kubectl get pods -n task-runner
kubectl get svc task-runner-api -n task-runner

# Call the API via NodePort
HOST=$(minikube ip)
curl -s http://$HOST:30080/api/tasks | python3 -m json.tool

# Create a task and run it
curl -i -X PUT http://$HOST:30080/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{"id":"123","name":"Hello","owner":"You","command":"echo hello from busybox"}'

curl -i -X PUT http://$HOST:30080/api/tasks/123/executions
```

Notes:
- Deployment container port is `8081`. Services use `targetPort: 8081` (already aligned).
- Image name expected by the deployment: `task-runner-api:podrunner`.
- Namespace used throughout: `task-runner`.
- RBAC manifests grant the API permission to create/delete pods and read pod logs.

## REST API Summary
- `GET  /api/tasks` — list or `?id=...` fetch by id
- `PUT  /api/tasks` — upsert task (JSON body)
- `DELETE /api/tasks/{id}` — delete task
- `GET  /api/tasks/search?name=...` — search by name (404 if none)
- `PUT  /api/tasks/{id}/executions` — run task in K8s pod, returns execution (start/end/output)

## Troubleshooting
- Maven not on PATH: install Maven; re-run build.
- Minikube Docker driver not running: restart Docker; `minikube start` again.
- NodePort unreachable: check `minikube ip` and service `30080`.
- Mongo connectivity: verify `SPRING_DATA_MONGODB_URI` or Mongo StatefulSet readiness.
- RBAC errors: confirm ServiceAccount/Role/RoleBinding applied in `task-runner`.
- Pod never finishes: increase `RUNNER_JOB_TIMEOUT_SECONDS` or check pod logs.
