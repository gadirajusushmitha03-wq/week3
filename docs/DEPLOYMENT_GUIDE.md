# SecureCollab Deployment Guide

## Table of Contents
1. [Local Development (Docker Compose)](#local-development)
2. [Kubernetes Production Deployment](#kubernetes-production)
3. [Security Hardening](#security-hardening)
4. [Monitoring & Observability](#monitoring--observability)
5. [Backup & Disaster Recovery](#backup--disaster-recovery)

---

## Local Development

### Prerequisites
- Docker Desktop 4.0+
- Docker Compose 2.0+
- Java 21 (for local development)
- Maven 3.8+

### Quick Start

```bash
# Clone the repository
git clone https://github.com/your-org/securecollab.git
cd securecollab

# Build all services
mvn clean package -DskipTests

# Start all services with Docker Compose
docker-compose up -d

# Verify services are running
curl http://localhost:8080/actuator/health

# View logs
docker-compose logs -f
```

**Service Endpoints (Local):**
- Gateway: http://localhost:8080
- Chat Service: http://localhost:8081/swagger-ui.html
- WebSocket: ws://localhost:8082/ws
- PostgreSQL: localhost:5432
- Redis: localhost:6379
- RabbitMQ Management: http://localhost:15672 (guest/guest)
- Kafka: localhost:9092

---

## Kubernetes Production Deployment

### Prerequisites
- Kubernetes 1.25+ cluster
- Helm 3.0+
- kubectl configured with prod cluster access
- Container registry (Docker Hub, ECR, GCR, etc.)

### Step 1: Build and Push Images

```bash
# Build images
docker build -t your-registry/chat-service:1.0.0 ./chat-service
docker build -t your-registry/gateway-service:1.0.0 ./gateway-service
docker build -t your-registry/websocket-service:1.0.0 ./websocket-service

# Push to registry
docker push your-registry/chat-service:1.0.0
docker push your-registry/gateway-service:1.0.0
docker push your-registry/websocket-service:1.0.0
```

### Step 2: Deploy with Helm

```bash
# Create namespace
kubectl create namespace securecollab

# Add Helm repositories (optional, for bitnami charts)
helm repo add bitnami https://charts.bitnami.com/bitnami

# Deploy with custom values
helm install securecollab ./helm-chart \
  -n securecollab \
  -f helm-chart-values.yaml \
  --set image.repository=your-registry \
  --set image.tag=1.0.0
```

### Step 3: Configure Ingress (TLS)

```bash
# Install cert-manager for automatic TLS
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.10.0/cert-manager.yaml

# Create ClusterIssuer for Let's Encrypt
kubectl apply -f - <<EOF
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: admin@example.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
EOF
```

### Step 4: Verify Deployment

```bash
# Check pods running
kubectl get pods -n securecollab

# Check services
kubectl get svc -n securecollab

# Get ingress IP
kubectl get ingress -n securecollab

# View logs of a specific pod
kubectl logs -n securecollab deployment/chat-service

# Port-forward for local testing
kubectl port-forward -n securecollab svc/gateway-service 8080:8080
```

---

## Security Hardening

### 1. Network Policies

```bash
# Deny all ingress by default, then allow specific traffic
kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny
  namespace: securecollab
spec:
  podSelector: {}
  policyTypes:
  - Ingress

---
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-gateway
  namespace: securecollab
spec:
  podSelector:
    matchLabels:
      app: gateway-service
  policyTypes:
  - Ingress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
EOF
```

### 2. RBAC (Role-Based Access Control)

```bash
# Create service account with limited permissions
kubectl apply -f - <<EOF
apiVersion: v1
kind: ServiceAccount
metadata:
  name: securecollab-app
  namespace: securecollab

---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: securecollab-app-role
  namespace: securecollab
rules:
- apiGroups: [""]
  resources: ["secrets"]
  verbs: ["get", "list"]
- apiGroups: [""]
  resources: ["configmaps"]
  verbs: ["get", "list"]

---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: securecollab-app-binding
  namespace: securecollab
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: securecollab-app-role
subjects:
- kind: ServiceAccount
  name: securecollab-app
  namespace: securecollab
EOF
```

### 3. Secrets Management (HashiCorp Vault)

```bash
# Install Vault
helm repo add hashicorp https://helm.releases.hashicorp.com
helm install vault hashicorp/vault -n vault --create-namespace

# Store OAuth secrets in Vault
vault kv put secret/securecollab/oauth \
  jira_client_id="xxx" \
  jira_client_secret="yyy" \
  github_token="zzz"
```

### 4. Pod Security Standards

```bash
# Enforce restricted pod security
kubectl label namespace securecollab \
  pod-security.kubernetes.io/enforce=restricted \
  pod-security.kubernetes.io/audit=restricted \
  pod-security.kubernetes.io/warn=restricted
```

### 5. Encryption at Rest (etcd)

```bash
# Enable encryption for etcd (cluster-level config)
# Add to kube-apiserver:
# --encryption-provider-config=/etc/kubernetes/encryption/config.yaml
```

---

## Monitoring & Observability

### Deploy Prometheus + Grafana

```bash
# Add Prometheus Helm repo
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install Prometheus
helm install prometheus prometheus-community/kube-prometheus-stack \
  -n monitoring --create-namespace \
  -f - <<EOF
prometheus:
  prometheusSpec:
    serviceMonitorSelectorNilUsesHelmValues: false
EOF

# Port-forward Grafana
kubectl port-forward -n monitoring svc/prometheus-community-grafana 3000:80

# Login: admin / prom-operator
# Add data source: http://prometheus-community-kube-prom-prometheus:9090
# Import dashboard: grafana-dashboard.json
```

### View Key Metrics

```bash
# Open Prometheus UI
kubectl port-forward -n monitoring svc/prometheus-community-kube-prom-prometheus 9090:9090

# Query: rate(chat.messages.sent[5m])
# Query: histogram_quantile(0.95, chat.message.latency)
# Query: up{job="chat-service"}
```

### Setup Alerting

```bash
# Install AlertManager
kubectl apply -f - <<EOF
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: securecollab-alerts
  namespace: monitoring
spec:
  groups:
  - name: securecollab
    interval: 30s
    rules:
    - alert: HighMessageLatency
      expr: histogram_quantile(0.95, chat.message.latency) > 5000
      for: 5m
      labels:
        severity: warning
      annotations:
        summary: "High message latency detected"
EOF
```

---

## Backup & Disaster Recovery

### Database Backups

```bash
# Automated daily backups (via CronJob)
kubectl apply -f - <<EOF
apiVersion: batch/v1
kind: CronJob
metadata:
  name: postgres-backup
  namespace: securecollab
spec:
  schedule: "0 2 * * *"  # 2 AM daily
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: postgres:15
            env:
            - name: PGPASSWORD
              valueFrom:
                secretKeyRef:
                  name: postgres-credentials
                  key: password
            command:
            - /bin/sh
            - -c
            - pg_dump -h postgres -U postgres securecollab | gzip > /backups/securecollab-$(date +%Y%m%d).sql.gz
            volumeMounts:
            - name: backups
              mountPath: /backups
          volumes:
          - name: backups
            persistentVolumeClaim:
              claimName: postgres-backups
          restartPolicy: OnFailure
EOF
```

### Restore from Backup

```bash
# List available backups
kubectl exec -n securecollab postgres-pod -- ls -la /backups/

# Restore database
kubectl exec -n securecollab postgres-pod -- gunzip < /backups/securecollab-20240115.sql.gz | psql -U postgres securecollab
```

### PersistentVolume Strategy

```yaml
# Use managed storage (EBS, GCS, etc.)
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgres-data
  namespace: securecollab
spec:
  accessModes:
    - ReadWriteOnce
  storageClassName: fast-ssd  # Use cloud-optimized storage
  resources:
    requests:
      storage: 100Gi
```

---

## Scaling & Performance Tuning

### Horizontal Scaling

```bash
# Scale chat-service to 5 replicas
kubectl scale deployment/chat-service --replicas=5 -n securecollab

# Auto-scaling based on CPU
kubectl autoscale deployment chat-service --min=2 --max=10 --cpu-percent=70 -n securecollab
```

### Database Connection Pooling

```yaml
# In application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

### Cache Warming

```bash
# Pre-load frequently accessed data into Redis
redis-cli --from-profile production BGSAVE
```

---

## Maintenance & Updates

### Rolling Updates

```bash
# Update service image
kubectl set image deployment/chat-service \
  chat-service=your-registry/chat-service:1.1.0 \
  -n securecollab

# Monitor rollout
kubectl rollout status deployment/chat-service -n securecollab

# Rollback if needed
kubectl rollout undo deployment/chat-service -n securecollab
```

### Database Migrations

```bash
# Run Flyway migrations before deployment
./mvn flyway:migrate -Dflyway.url=jdbc:postgresql://postgres:5432/securecollab
```

---

## Troubleshooting

### Pod fails to start
```bash
kubectl describe pod <pod-name> -n securecollab
kubectl logs <pod-name> -n securecollab --previous
```

### Service unreachable
```bash
kubectl get svc -n securecollab
kubectl port-forward svc/gateway-service 8080:8080 -n securecollab
```

### Memory/CPU issues
```bash
kubectl top nodes
kubectl top pods -n securecollab
```

---

## References
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Helm Best Practices](https://helm.sh/docs/chart_best_practices/)
- [Spring Boot Production Guide](https://spring.io/guides/gs/spring-boot-docker/)
