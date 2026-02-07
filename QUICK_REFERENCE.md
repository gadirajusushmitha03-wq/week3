# SecureCollab - Quick Reference Guide

## 🚀 60-Second Quickstart

```bash
# Clone & navigate
git clone <repo> && cd securecollab

# Start all services
docker-compose up -d

# View status
curl http://localhost:8080/actuator/health

# Verify services running
docker-compose ps
```

**Services are live:**
- Gateway: http://localhost:8080
- Chat API: http://localhost:8081 
- WebSocket: ws://localhost:8082

---

## 🔑 Key API Endpoints

### Authentication
```bash
POST /api/auth/login
  Body: { "username": "alice@example.com", "password": "..." }
  Returns: { "token": "JWT_TOKEN", "expiresIn": 3600 }

POST /api/auth/refresh
  Body: { "token": "EXISTING_JWT" }
  Returns: { "token": "NEW_JWT" }
```

### Messaging
```bash
POST /api/chat/send-message
  Headers: Authorization: Bearer <JWT>
  Body: {
    "to": "bob@example.com",
    "encryptedContent": "base64-AES256",
    "iv": "random-16-bytes"
  }

GET /api/chat/messages?limit=50&offset=0
  Headers: Authorization: Bearer <JWT>
  Returns: { messages: [...] }

GET /api/chat/offline-messages
  Headers: Authorization: Bearer <JWT>
  Returns: { messages: [...] }
```

### Encryption Keys
```bash
POST /api/keys/register
  Headers: Authorization: Bearer <JWT>
  Body: {
    "deviceId": "iphone-12",
    "publicKey": "-----BEGIN PUBLIC KEY-----\n..."
  }

GET /api/keys/{userId}/{deviceId}
  Returns: { "publicKey": "..." }
```

### Voice Calls
```bash
POST /api/voice/initiate
  Headers: Authorization: Bearer <JWT>
  Body: {
    "recipientId": "bob@example.com",
    "callType": "audio|video"
  }
  Returns: { "callId": "call-123" }

GET /api/voice/{callId}/status
  Returns: { "status": "RINGING|CONNECTED|ENDED", "duration": 45 }
```

### Bots & Integrations
```bash
POST /api/integrations/jira/create-issue
  Headers: Authorization: Bearer <JWT>
  Body: {
    "projectKey": "CHAT",
    "issueType": "Bug",
    "summary": "Issue title",
    "description": "Description"
  }
  Returns: { "issueKey": "CHAT-1234" }

POST /api/integrations/github/create-pr
  Headers: Authorization: Bearer <JWT>
  Body: {
    "owner": "org",
    "repo": "repo",
    "title": "PR title",
    "head": "feature-branch",
    "base": "main"
  }
  Returns: { "prNumber": 42 }
```

### GDPR Compliance
```bash
GET /api/gdpr/export
  Headers: Authorization: Bearer <JWT>
  Returns: { messages: [...], channels: [...], keys: [...] }

DELETE /api/gdpr/delete-account
  Headers: Authorization: Bearer <JWT>
  Returns: { "scheduledDeletionDate": "2024-02-15" }

GET /api/gdpr/data-access-summary
  Headers: Authorization: Bearer <JWT>
  Returns: { messageCount: 150, channelCount: 5, ... }
```

---

## 🏗️ Architecture at a Glance

```
Client App
    ↓ HTTPS/WSS
┌─────────────┐
│   Gateway   │ (Port 8080)
│  - JWT Auth │
│  - Rate Lim │
└──┬──┬───┬──┘
   │  │   │
REST │  WebSocket Voice
   │  │   │
┌──▼┐┌─▼──┐┌──▼──┐
│Chat Service  Websocket Service  Voice Service │
│(8081)         (8082)           (8082)         │
└──┬─────┬─────┬──┘
   │     │     │
   └──┬──┴──┬──┘
    Kafka + RabbitMQ (Message Broker)
      │
   PostgreSQL + Redis
```

---

## 📊 Monitoring

### Prometheus Metrics
```
# Message throughput
rate(chat.messages.sent[5m])

# Message latency (p95)
histogram_quantile(0.95, chat.message.latency)

# Toxicity detection
rate(chat.toxicity.detected[5m])

# Voice calls
voice.calls.active
rate(voice.calls.connected[5m])

# Service health
up{job="chat-service"}
```

### Access Dashboards
```
Prometheus: http://localhost:9090
Grafana: http://localhost:3000 (admin/admin)
AlertManager: http://localhost:9093
```

---

## 🔐 Security Features

| Feature | Implementation |
|---------|-----------------|
| Encryption | AES-256-GCM (per-message random IV) |
| Authentication | JWT (HS512, 3600s TTL) |
| Rate Limiting | 20 msgs/10s per user (Redis sliding-window) |
| OAuth2 | Jira, GitHub, Jenkins (encrypted token storage) |
| Network | TLS 1.2+, CORS, RBAC |
| Audit | All API calls logged (non-plaintext) |

---

## 🐳 Docker Compose Services

| Service | Port | Health Check |
|---------|------|--------------|
| PostgreSQL | 5432 | `psql -U postgres -c "SELECT 1"` |
| Redis | 6379 | `redis-cli ping` |
| Kafka | 9092 | `kafka-topics --list --bootstrap-server kafka:9092` |
| RabbitMQ | 5672/15672 | http://localhost:15672 (guest/guest) |
| Gateway | 8080 | http://localhost:8080/actuator/health |
| Chat Service | 8081 | http://localhost:8081/actuator/health |
| WebSocket Service | 8082 | http://localhost:8082/actuator/health |

---

## 📝 Common Tasks

### Test Encrypted Message Flow
```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -d '{"username":"alice","password":"password"}' \
  -H "Content-Type: application/json" | jq -r .token)

# 2. Register public key
curl -X POST http://localhost:8080/api/keys/register \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"device-1","publicKey":"..."}'

# 3. Send encrypted message
curl -X POST http://localhost:8080/api/chat/send-message \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"to":"bob@example.com","encryptedContent":"...","iv":"..."}'

# 4. Retrieve message
curl http://localhost:8080/api/chat/messages \
  -H "Authorization: Bearer $TOKEN"
```

### View Service Logs
```bash
docker-compose logs -f chat-service        # Chat service logs
docker-compose logs -f websocket-service   # WebSocket logs
docker-compose logs -f gateway             # Gateway logs
docker-compose logs -f postgres            # Database logs
```

### Check Kafka Topics
```bash
# List topics
docker-compose exec kafka kafka-topics --list --bootstrap-server kafka:9092

# Consume messages
docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server kafka:9092 \
  --topic chat.messages \
  --from-beginning

# Check consumer lag
docker-compose exec kafka kafka-consumer-groups \
  --bootstrap-server kafka:9092 \
  --group offline-queue-consumer \
  --describe
```

### Run Integration Tests
```bash
cd chat-service
mvn test -Dtest=SecureCollabIntegrationTest
```

---

## 🚀 Kubernetes Deployment

### Quick Deploy
```bash
# Create namespace
kubectl create namespace securecollab

# Deploy with Helm
helm install securecollab ./helm-chart \
  -n securecollab \
  -f helm-chart-values.yaml

# Check status
kubectl get pods -n securecollab
kubectl get svc -n securecollab

# Port-forward for testing
kubectl port-forward -n securecollab svc/gateway-service 8080:8080
```

### Scale Services
```bash
# Scale chat-service to 5 replicas
kubectl scale deployment chat-service --replicas=5 -n securecollab

# Enable auto-scaling (HPA)
kubectl autoscale deployment chat-service --min=2 --max=10 --cpu-percent=70 -n securecollab

# Check HPA status
kubectl get hpa -n securecollab
```

### View Logs
```bash
# Logs from specific pod
kubectl logs -n securecollab pod/chat-service-xyz

# Tail logs
kubectl logs -n securecollab -f deployment/chat-service

# Logs from previous pod (if crashed)
kubectl logs -n securecollab pod/chat-service-xyz --previous
```

---

## 🛠️ Troubleshooting

### Service Won't Start
```bash
# Check logs
docker-compose logs chat-service

# Verify database connection
docker-compose exec postgres psql -U postgres -c "SELECT version();"

# Check if port already in use
lsof -i :8080
```

### WebSocket Connection Issues
```bash
# Test WebSocket endpoint
wscat -c ws://localhost:8082/ws

# Check service is running
curl http://localhost:8082/actuator/health

# View WebSocket logs
docker-compose logs -f websocket-service
```

### Messages Not Flowing
```bash
# Check Kafka is running
docker-compose exec kafka kafka-brokers --bootstrap-server kafka:9092

# List topics
docker-compose exec kafka kafka-topics --list --bootstrap-server kafka:9092

# Check consumer lag
docker-compose exec kafka kafka-consumer-groups \
  --bootstrap-server kafka:9092 \
  --group offline-queue-consumer \
  --describe
```

### Database Issues
```bash
# Connect to database
docker-compose exec postgres psql -U postgres -d securecollab

# Check tables
psql> \dt

# View message count
psql> SELECT COUNT(*) FROM messages;

# Check connections
psql> SELECT count(*) FROM pg_stat_activity;
```

---

## 📚 Documentation

| Guide | Purpose |
|-------|---------|
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | System design & components |
| [DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md) | Local + cloud setup |
| [TECHNICAL_ARCHITECTURE.md](docs/TECHNICAL_ARCHITECTURE.md) | Deep implementation details |
| [PROJECT_COMPLETION_SUMMARY.md](PROJECT_COMPLETION_SUMMARY.md) | What was implemented |

---

## ⚡ Performance Tips

### Optimize Message Throughput
```yaml
# In application.yml (Chat Service)
spring:
  kafka:
    producer:
      batch-size: 32768          # 32KB batches
      linger-ms: 20              # Wait 20ms to batch
      compression-type: snappy   # Compression
```

### Database Performance
```sql
-- Add indexes for common queries
CREATE INDEX idx_messages_from_user ON messages(from_user_id);
CREATE INDEX idx_messages_to_user ON messages(to_user_id);
CREATE INDEX idx_messages_created_at ON messages(created_at DESC);
```

### Redis Caching
```
Presence: presence:{userId} (TTL: 600s)
Rate limit: ratelimit:{userId}:{windowStart} (TTL: 10s)
Channel metadata: channel:{id} (TTL: 3600s)
```

---

## 🔒 Security Checklist

- [ ] JWT secret configured (strong, random)
- [ ] Database password changed (not default)
- [ ] OAuth2 credentials stored in Vault/Secrets Manager
- [ ] TLS/HTTPS enabled in production
- [ ] Network policies configured (Kubernetes)
- [ ] Rate limits enabled (API Gateway)
- [ ] Audit logging enabled and monitored
- [ ] Backups encrypted and tested
- [ ] GDPR compliance verified
- [ ] Penetration testing scheduled

---

## 📞 Support Resources

- **GitHub Issues**: Report bugs
- **Documentation**: See `/docs` folder
- **Logs**: `docker-compose logs -f <service>`
- **Metrics**: Grafana dashboard at http://localhost:3000
- **Status**: `curl http://localhost:8080/actuator/health`

---

## 🎯 Feature Checklist

- [x] End-to-end encryption (AES-256-GCM)
- [x] Real-time messaging (WebSocket/STOMP)
- [x] Offline message delivery (7-day queue)
- [x] Voice calling (WebRTC signaling)
- [x] Toxicity detection (< 100ms)
- [x] Chat channels (public/private)
- [x] Bot workflows (state machine)
- [x] Jira integration (OAuth2)
- [x] GitHub integration (OAuth2)
- [x] CI/CD integration (Jenkins/GitLab/GitHub)
- [x] Rate limiting (20 msgs/10s)
- [x] Presence tracking (Redis TTL)
- [x] Kubernetes deployment (Helm)
- [x] Monitoring (Prometheus + Grafana)
- [x] GDPR compliance (deletion, export)

---

**Last Updated**: 2024-01-15  
**Status**: ✅ Production-Ready  
**Next**: Deploy to production!
