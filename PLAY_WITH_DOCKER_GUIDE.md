# 🐳 Play with Docker - SecureCollab Verification Guide

**Repository**: https://github.com/sushmitha0204/Week3/

---

## 🚀 Quick Start (5 minutes)

### Step 1: Open Play with Docker
Go to **https://labs.play-with-docker.com/** and click **START**

### Step 2: Clone Repository
```bash
git clone https://github.com/sushmitha0204/Week3.git
cd Week3
```

### Step 3: Start All Services
```bash
docker-compose up -d
```

### Step 4: Wait for Services (2-3 minutes)
```bash
sleep 120
docker-compose ps
```

### Step 5: Check Service Status Before Health Checks
```bash
# First, make sure all containers are UP
docker-compose ps

# If any are not running, check logs
docker-compose logs

# If services failed, rebuild and restart
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

### Step 6: Verify All Services Running
```bash
# Check final status - should show all services as UP
docker-compose ps

# View recent logs for any errors
docker-compose logs --tail=50
```

---

## ✅ Verification Commands

### ⚠️ IMPORTANT: Wait for Full Startup
Services need time to initialize. After `docker-compose up -d`:
- **3-5 minutes**: Database initialization
- **5-8 minutes**: All services fully ready
- **First 2 minutes**: Some 404s are normal (still starting)

### Health Check All Services
```bash
# STEP 1: Verify all containers are running
docker-compose ps

# Expected output should show all services with status "Up"
# If any show "Exit" or "Exited", services need debugging

# STEP 2: Give them more time if they just started (wait 1-2 minutes)
sleep 120

# STEP 3: Check logs for startup errors
docker-compose logs --tail=100

# STEP 4: Now try health checks
# Gateway Service (Port 8080)
curl http://localhost:8080/actuator/health

# Chat Service (Port 8081)
curl http://localhost:8081/actuator/health

# WebSocket Service (Port 8082)
curl http://localhost:8082/actuator/health

# PostgreSQL (Port 5432)
docker-compose exec postgres pg_isready -U postgres

# Redis (Port 6379)
docker-compose exec redis redis-cli PING

# Kafka (Port 9092) - Simple connectivity test
docker-compose exec kafka nc -zv localhost 9092

# RabbitMQ (Port 5672)
docker-compose exec rabbitmq rabbitmq-diagnostics -q ping
```

### View Service Logs
```bash
# All services
docker-compose logs --tail=50

# Specific service
docker-compose logs -f chat-service
docker-compose logs -f websocket-service
docker-compose logs -f gateway-service
```

### Check Docker Compose Status
```bash
docker-compose ps
```

---

## 🧪 Functional Tests

### 1. Authentication Test
```bash
curl -X POST http://localhost:8080/api/chat/login \
  -H "Content-Type: application/json" \
  -d '{
    "username":"testuser",
    "password":"password123"
  }'
```

**Expected**: 200 OK with JWT token

---

### 2. Channel Creation Test
```bash
curl -X POST http://localhost:8081/api/chat/channels \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "channelName": "general",
    "description": "General chat channel",
    "isPrivate": false
  }'
```

**Expected**: 201 Created with channel details

---

### 3. Message Send Test
```bash
curl -X POST http://localhost:8081/api/chat/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "channelId": "general",
    "content": "Hello SecureCollab!",
    "encryptionKey": "your_encryption_key"
  }'
```

**Expected**: 201 Created with encrypted message

---

### 4. Toxicity Detection Test
```bash
curl -X POST http://localhost:8081/api/chat/toxicity/check \
  -H "Content-Type: application/json" \
  -d '{
    "text": "This is a normal message"
  }'
```

**Expected**: 200 OK with toxicity score (0-100)

---

### 5. Bot Workflow Test
```bash
curl -X POST http://localhost:8081/api/bot/workflows \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "workflowName": "message_trigger",
    "trigger": "MESSAGE_CREATED",
    "action": "CREATE_TICKET",
    "config": {
      "system": "JIRA",
      "project": "TEST"
    }
  }'
```

**Expected**: 201 Created with workflow ID

---

### 6. Reminder Test
```bash
curl -X POST http://localhost:8081/api/chat/reminders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": "testuser",
    "channelId": "general",
    "title": "Team Standup",
    "description": "Daily standup meeting",
    "remindAt": "2026-02-07T10:00:00"
  }'
```

**Expected**: 201 Created with reminder ID

---

### 7. Approval Request Test
```bash
curl -X POST http://localhost:8081/api/chat/approvals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "requesterId": "user1",
    "channelId": "general",
    "title": "Merge Production Deployment",
    "description": "Approve production deploy",
    "approverIds": ["user2", "user3"]
  }'
```

**Expected**: 201 Created with approval request ID

---

### 8. Encryption Test
```bash
curl -X POST http://localhost:8081/api/encryption/encrypt \
  -H "Content-Type: application/json" \
  -d '{
    "plaintext": "Sensitive message",
    "userId": "testuser"
  }'
```

**Expected**: 200 OK with encrypted ciphertext (Base64)

---

### 9. Offline Queue Test
```bash
curl -X GET http://localhost:8081/api/chat/offline-queue/user1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected**: 200 OK with list of offline messages

---

### 10. File Sharing Test
```bash
curl -X POST http://localhost:8081/api/chat/files/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@test.txt" \
  -F "channelId=general" \
  -F "encryptionKey=key123"
```

**Expected**: 201 Created with file metadata

---

## 📊 Monitoring Access

### Prometheus Metrics
```bash
# Available at port 9090
curl http://localhost:9090/api/v1/targets

# Query metrics
curl 'http://localhost:9090/api/v1/query?query=up'
```

### Grafana Dashboards
```bash
# Available at port 3000
# Login: admin/admin
# Then access via browser: http://localhost:3000
```

---

## 🔧 Troubleshooting

### Services Returning 404 Errors
```bash
# This means the service is running but the endpoint path is wrong
# First check if services are actually UP
docker-compose ps

# Look for issues in logs
docker-compose logs chat-service
docker-compose logs websocket-service
docker-compose logs gateway-service

# Common reasons:
# 1. Application didn't start correctly - check Spring Boot logs
# 2. Actuator endpoints not configured
# 3. Service still initializing - wait 30 more seconds

# Try these alternative health endpoints
curl http://localhost:8080/health
curl http://localhost:8081/health
curl http://localhost:8082/health

# Or check if services are just slow to start
sleep 60
docker-compose logs --tail=50
```

### PostgreSQL Service Not Running
```bash
# Check PostgreSQL status
docker-compose ps postgres

# Check PostgreSQL logs
docker-compose logs postgres

# If it's not running, restart all services
docker-compose restart

# If still failing, rebuild
docker-compose down
docker-compose build --no-cache
docker-compose up -d

# Wait longer (5 minutes) for database to initialize
sleep 300
docker-compose ps
```
```bash
# Check logs
docker-compose logs

# Restart services
docker-compose restart

# Rebuild images
docker-compose build --no-cache
docker-compose up -d
```

### Port Conflicts
```bash
# Check which ports are in use
docker ps

# See port mappings
docker-compose ps

# Check specific port
netstat -an | grep 8080
```

### Database Connection Issues
```bash
# Check PostgreSQL logs
docker-compose logs postgres

# Test connection
docker-compose exec postgres psql -U postgres -d securecollab -c "SELECT 1"
```

### Kafka Issues
```bash
# Check Kafka logs
docker-compose logs kafka

# Simple connectivity test (Port is open and responsive)
docker-compose exec kafka nc -zv localhost 9092

# Alternative: Check if Kafka container is running
docker-compose ps kafka
```

### Redis Issues
```bash
# Check Redis logs
docker-compose logs redis

# Test Redis
docker-compose exec redis redis-cli
> PING
> FLUSHALL
> EXIT
```

---

## 📈 Performance Verification

### Message Throughput
```bash
# Send 100 messages and measure time
time for i in {1..100}; do
  curl -X POST http://localhost:8081/api/chat/messages \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer YOUR_JWT_TOKEN" \
    -d "{\"channelId\": \"general\", \"content\": \"Message $i\"}"
done
```

**Expected**: < 10 seconds for 100 messages

---

### Latency Check
```bash
# Measure message latency
curl -w "Response time: %{time_total}s\n" \
  -X POST http://localhost:8081/api/chat/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"channelId": "general", "content": "Latency test"}'
```

**Expected**: < 500ms (p95)

---

### Resource Usage
```bash
# Check CPU/Memory
docker stats

# Check disk usage
docker system df

# See service resource limits
docker-compose ps

# Monitor in real-time
docker stats --no-stream=false
```

---

## 🎯 Complete Verification Checklist

- [ ] All 7 containers running (`docker-compose ps`)
- [ ] Gateway responds to health check (port 8080)
- [ ] Chat Service responds to health check (port 8081)
- [ ] WebSocket responds to health check (port 8082)
- [ ] PostgreSQL database connected
- [ ] Redis cache connected
- [ ] Kafka broker accessible
- [ ] RabbitMQ accessible
- [ ] Authentication works (JWT token obtained)
- [ ] Channel creation works
- [ ] Message encryption works
- [ ] Toxicity detection works
- [ ] Bot workflows triggerable
- [ ] Reminders can be created
- [ ] Approval requests functional
- [ ] File sharing works
- [ ] Prometheus metrics available
- [ ] Grafana dashboards accessible
- [ ] No critical errors in logs
- [ ] Message latency < 500ms

---

## 🚀 Next Steps

1. **Monitor Dashboard**: Open Grafana at http://localhost:3000
2. **Test WebSocket**: Use WebSocket client to test real-time messaging
3. **Trigger Bots**: Send messages to trigger bot workflows
4. **Check Logs**: Monitor service logs in real-time
5. **Load Test**: Test with multiple concurrent users

---

## 📞 Support

If services don't start:
1. Check logs: `docker-compose logs`
2. Restart: `docker-compose restart`
3. Rebuild: `docker-compose build --no-cache && docker-compose up -d`
4. View docker-compose.yml for configuration

---

**Status**: ✅ Ready to verify in Play with Docker
**Repository**: https://github.com/sushmitha0204/Week3/
**Date**: February 7, 2026
