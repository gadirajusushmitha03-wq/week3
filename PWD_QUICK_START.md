# SecureCollab - PWD Quick Start Guide

## Prerequisites
- Docker Hub account (free at https://hub.docker.com)
- Play with Docker session (https://labs.play-with-docker.com)
- Your Docker images pushed to Docker Hub

---

## STEP 1: Access Play with Docker

1. Go to https://labs.play-with-docker.com
2. Click **"Login with Docker"**
3. Sign in with Docker credentials
4. Click **"+ ADD NEW INSTANCE"** to start
5. You'll get a terminal with ~4 hours session time

---

## STEP 2: Clone Repository

```bash
git clone https://github.com/YOUR_GITHUB_USERNAME/week3pip.git
cd week3pip
```

If no GitHub repo, create one or use the local files:
```bash
# Option: Copy files manually if repo not available
# For now, we'll proceed with docker-compose.yml present
```

---

## STEP 3: Verify Files

```bash
ls -la
```

You should see:
- `docker-compose.yml`
- `chat-service/` folder
- `websocket-service/` folder
- `gateway-service/` folder
- `frontend/` folder

---

## STEP 4: Update docker-compose.yml

Replace image names with your Docker Hub username:

```bash
nano docker-compose.yml
```

Find and update these sections:

**BEFORE:**
```yaml
image: week3pip-chat-service:latest
image: week3pip-websocket-service:latest
image: week3pip-gateway-service:latest
```

**AFTER:**
```yaml
image: YOUR_DOCKERHUB_USERNAME/securecolllab-chat-service:latest
image: YOUR_DOCKERHUB_USERNAME/securecolllab-websocket-service:latest
image: YOUR_DOCKERHUB_USERNAME/securecolllab-gateway-service:latest
```

**Or use sed to replace:**
```bash
sed -i 's|week3pip-chat-service|YOUR_DOCKERHUB_USERNAME/securecolllab-chat-service|g' docker-compose.yml
sed -i 's|week3pip-websocket-service|YOUR_DOCKERHUB_USERNAME/securecolllab-websocket-service|g' docker-compose.yml
sed -i 's|week3pip-gateway-service|YOUR_DOCKERHUB_USERNAME/securecolllab-gateway-service|g' docker-compose.yml
```

**Verify changes:**
```bash
cat docker-compose.yml | grep image:
```

---

## STEP 5: Start Services

```bash
docker-compose up -d
```

This starts all services in detached mode:
- PostgreSQL (database)
- RabbitMQ (messaging)
- Kafka (streaming)
- Chat Service (port 8081)
- WebSocket Service (port 8082)
- Gateway Service (port 8080)
- Frontend (served by gateway)

---

## STEP 6: Wait for Services to Initialize

```bash
# Wait 30 seconds for services to start
sleep 30

# Check status
docker-compose ps
```

All services should show `Up` status. Example output:
```
NAME                    STATUS              PORTS
postgres                Up 2 minutes         5432/tcp
rabbitmq                Up 2 minutes         5672/tcp, 15672/tcp
kafka                   Up 2 minutes         9092/tcp
chat-service            Up About a minute    8081/tcp
websocket-service       Up About a minute    8082/tcp
gateway-service         Up About a minute    8080/tcp
```

---

## STEP 7: Verify Services are Running

### Check Logs (troubleshoot if needed):
```bash
# View all logs
docker-compose logs

# View specific service
docker-compose logs chat-service
docker-compose logs websocket-service
docker-compose logs gateway-service
```

### Test Service Health:
```bash
# Gateway service health
curl http://localhost:8080/actuator/health

# Chat service health
curl http://localhost:8081/actuator/health

# WebSocket service health
curl http://localhost:8082/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

---

## STEP 8: Access the Application

### In PWD, click the port number to open services:

1. **Gateway (Frontend)** - Click port 8080
   - This opens the chat interface
   - Load the frontend web application

2. **Chat API** - Click port 8081
   - Backend chat service
   - Test API endpoints

3. **WebSocket** - Click port 8082
   - Real-time messaging service
   - WebSocket connections

### Or manually access via:
```
Gateway: http://IP:8080
Chat API: http://IP:8081
WebSocket: http://IP:8082
```

---

## STEP 9: Test API Endpoints

### Create a Channel:
```bash
curl -X POST http://localhost:8080/chat/api/chat/channels \
  -H "Content-Type: application/json" \
  -d '{
    "name": "general",
    "description": "General discussion"
  }'
```

### Send a Message:
```bash
curl -X POST http://localhost:8081/api/chat/send \
  -H "Content-Type: application/json" \
  -d '{
    "channelId": "general",
    "senderId": "user1",
    "senderName": "Alice",
    "content": "Hello SecureCollab!",
    "isEncrypted": true
  }'
```

### Get Messages:
```bash
curl http://localhost:8081/api/chat/messages/general
```

### Get Channels:
```bash
curl http://localhost:8081/api/chat/channels
```

---

## STEP 10: Test Real-Time Features

### Using WebSocket (from a terminal):
```bash
# Install websocat if needed
apk add --no-cache websocat

# Connect to WebSocket
websocat ws://localhost:8082/ws/chat/user1

# Send a message (in the websocat terminal)
{"type": "MESSAGE", "content": "Test message from WebSocket"}

# You'll see responses in real-time
```

---

## STEP 11: Test Key Features

### 1. Encryption Test
```bash
curl -X POST http://localhost:8081/api/chat/send \
  -H "Content-Type: application/json" \
  -d '{
    "channelId": "general",
    "senderId": "user1",
    "senderName": "User One",
    "content": "This is encrypted",
    "isEncrypted": true
  }'
```

### 2. Voice Call Test
```bash
curl -X POST http://localhost:8081/api/calls/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "callerId": "user1",
    "receiverId": "user2",
    "callType": "voice"
  }'
```

### 3. File Sharing Test
```bash
# Upload a file
curl -X POST http://localhost:8081/api/files/upload \
  -F "file=@test.txt" \
  -F "channelId=general" \
  -F "uploadedBy=user1"
```

---

## STEP 12: Check Database

```bash
# List databases
docker-compose exec postgres psql -U securecollab -l

# Connect to securecollab database
docker-compose exec postgres psql -U securecollab -d securecollab

# In psql, run queries:
\dt                          # List all tables
SELECT * FROM messages;      # View messages
SELECT * FROM channels;      # View channels
SELECT * FROM users;         # View users
\q                           # Exit psql
```

---

## STEP 13: Monitor Services

### View Resource Usage:
```bash
docker stats
```

### View Real-time Logs:
```bash
# Follow logs for all services
docker-compose logs -f

# Follow specific service
docker-compose logs -f chat-service
```

### Check Container Details:
```bash
docker-compose ps -a
docker inspect CONTAINER_ID
```

---

## STEP 14: Troubleshooting

### Services Not Starting
```bash
# Check logs
docker-compose logs SERVICE_NAME

# Restart services
docker-compose restart

# Full reset
docker-compose down -v
docker-compose up -d
```

### Port Already in Use
```bash
# Kill existing container
docker-compose down

# Or change ports in docker-compose.yml
# gateway: 9080:8080
# chat-service: 9081:8081
# websocket-service: 9082:8082
```

### Network Issues
```bash
# Test connectivity between containers
docker-compose exec chat-service ping websocket-service
docker-compose exec websocket-service ping postgres

# Check network
docker network ls
docker network inspect week3pip_default
```

### Database Connection Failed
```bash
# Check PostgreSQL logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres
```

### RabbitMQ Issues
```bash
# Check RabbitMQ logs
docker-compose logs rabbitmq

# Access RabbitMQ Management UI
# http://localhost:15672 (guest/guest)
```

---

## STEP 15: Stop and Cleanup

### Stop All Services:
```bash
docker-compose stop
```

### Remove Containers (keep volumes):
```bash
docker-compose down
```

### Full Cleanup (remove everything):
```bash
docker-compose down -v
```

---

## Quick Reference

| Service | Port | URL | Purpose |
|---------|------|-----|---------|
| Gateway | 8080 | http://localhost:8080 | Frontend, routing |
| Chat Service | 8081 | http://localhost:8081 | Chat API |
| WebSocket | 8082 | http://localhost:8082 | Real-time messaging |
| PostgreSQL | 5432 | localhost:5432 | Database |
| RabbitMQ | 5672 | localhost:5672 | Message queue |
| RabbitMQ Admin | 15672 | http://localhost:15672 | RabbitMQ UI |
| Kafka | 9092 | localhost:9092 | Event streaming |

---

## Verification Checklist

- [ ] Repository cloned successfully
- [ ] docker-compose.yml updated with Docker Hub images
- [ ] All services are running (`docker-compose ps`)
- [ ] Gateway service accessible (http://localhost:8080)
- [ ] Chat API responding (http://localhost:8081/actuator/health)
- [ ] WebSocket service running (http://localhost:8082/actuator/health)
- [ ] Database connected (psql test)
- [ ] Can send messages via API
- [ ] WebSocket connections working
- [ ] Frontend loads and displays
- [ ] Encryption working end-to-end
- [ ] Logs show no critical errors

---

## Performance Notes

- Initial startup: ~30 seconds
- Message send latency: <100ms
- WebSocket connection: <50ms
- CPU usage: Low (under 5% normally)
- Memory usage: ~500MB-1GB total
- Database size: Grows with message volume

---

## Next Steps

1. Load test with multiple concurrent users
2. Test failover scenarios
3. Check database backup/recovery
4. Test scaling (add more service instances)
5. Security audit API endpoints
6. Performance profiling

