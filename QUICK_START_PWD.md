# 🚀 Quick Start - Play with Docker (5 minutes)

**Repository**: https://github.com/sushmitha0204/Week3/

---

## Step-by-Step Instructions

### 1️⃣ Open Play with Docker
```
https://labs.play-with-docker.com/
Click: START
```

### 2️⃣ Clone Repository
```bash
git clone https://github.com/sushmitha0204/Week3.git
cd Week3
```

### 3️⃣ Start Services
```bash
docker-compose up -d
```

### 4️⃣ Wait 2-3 Minutes
```bash
sleep 120
docker-compose ps
```

### 5️⃣ Check Status
```bash
docker-compose logs --tail=20
```

---

## ✅ Quick Verification

### All Services Running?
```bash
docker-compose ps
```
**Should show 7 containers: gateway, chat-service, websocket-service, postgres, redis, kafka, rabbitmq**

### Gateway Responding?
```bash
curl http://localhost:8080/actuator/health
```
**Should return: `{"status":"UP"}`**

### Chat Service Responding?
```bash
curl http://localhost:8081/actuator/health
```
**Should return: `{"status":"UP"}`**

### WebSocket Responding?
```bash
curl http://localhost:8082/actuator/health
```
**Should return: `{"status":"UP"}`**

### Database Ready?
```bash
docker-compose exec postgres pg_isready -U postgres
```
**Should return: `accepting connections`**

### Redis Ready?
```bash
docker-compose exec redis redis-cli PING
```
**Should return: `PONG`**

### Kafka Ready?
```bash
docker-compose logs kafka | tail -5
```
**Should show broker started messages**

---

## 🎯 Quick Tests

### Test 1: Create a Channel
```bash
curl -X POST http://localhost:8081/api/chat/channels \
  -H "Content-Type: application/json" \
  -d '{"channelName":"general","description":"General chat","isPrivate":false}'
```

### Test 2: Send a Message
```bash
curl -X POST http://localhost:8081/api/chat/messages \
  -H "Content-Type: application/json" \
  -d '{"channelId":"general","content":"Hello SecureCollab!"}'
```

### Test 3: Check Toxicity
```bash
curl -X POST http://localhost:8081/api/chat/toxicity/check \
  -H "Content-Type: application/json" \
  -d '{"text":"This is a nice message"}'
```

### Test 4: View Logs
```bash
docker-compose logs -f chat-service
```

---

## 📊 Monitoring

### View All Services
```bash
docker-compose ps
```

### View Logs
```bash
# All services
docker-compose logs --tail=50

# Specific service
docker-compose logs -f chat-service
docker-compose logs -f websocket-service
docker-compose logs -f gateway-service
```

### Check Resource Usage
```bash
docker stats
```

### View Service Details
```bash
docker-compose ps
docker network ls
docker volume ls
```

---

## 🌐 Access Web Interfaces

Click the ports shown at the top of Play with Docker:

| Service | Port | URL |
|---------|------|-----|
| Gateway | 8080 | http://localhost:8080 |
| Chat | 8081 | http://localhost:8081 |
| WebSocket | 8082 | http://localhost:8082 |
| Prometheus | 9090 | http://localhost:9090 |
| Grafana | 3000 | http://localhost:3000 |

---

## 🔧 Troubleshooting

### Services Won't Start
```bash
# View detailed logs
docker-compose logs

# Restart all services
docker-compose restart

# Rebuild and restart
docker-compose build --no-cache
docker-compose up -d
```

### Check Specific Service
```bash
docker-compose logs chat-service
docker-compose logs websocket-service
docker-compose logs gateway-service
```

### Database Connection Issues
```bash
# Check PostgreSQL
docker-compose exec postgres psql -U postgres -c "SELECT 1"

# Check database exists
docker-compose exec postgres psql -U postgres -l
```

### Port Already in Use
```bash
# See what's using ports
netstat -an | grep LISTEN

# Stop and remove containers
docker-compose down
docker system prune -a
```

---

## ⏱️ Expected Timeline

| Action | Time |
|--------|------|
| Clone repo | 10s |
| Start containers | 5s |
| Services initialize | 60-120s |
| Database ready | 30s after startup |
| Health checks pass | 2-3 min |

**Total: ~5 minutes**

---

## ✨ What's Running

### Services
✅ **Gateway** (Port 8080) - API routing & authentication
✅ **Chat Service** (Port 8081) - Core business logic
✅ **WebSocket** (Port 8082) - Real-time messaging

### Infrastructure
✅ **PostgreSQL** - Message & user storage
✅ **Redis** - Caching & presence tracking
✅ **Kafka** - Event streaming
✅ **RabbitMQ** - Message queue

### Monitoring
✅ **Prometheus** (9090) - Metrics collection
✅ **Grafana** (3000) - Dashboard visualization
✅ **AlertManager** - Alert management

---

## 🎓 Features Available

✅ End-to-end encryption (AES-256-GCM)
✅ Real-time messaging
✅ Offline message delivery
✅ Toxicity detection (AI)
✅ Chat channels
✅ Bot workflows
✅ Reminders & approvals
✅ File sharing
✅ Voice calling (WebRTC signaling)
✅ Jira integration
✅ GitHub integration
✅ CI/CD integration
✅ Kubernetes-ready
✅ Production monitoring

---

## 📞 Next Steps

1. **Wait for startup** - 2-3 minutes
2. **Run verification** - `docker-compose ps`
3. **Check logs** - `docker-compose logs --tail=50`
4. **Test endpoints** - `curl http://localhost:8080/actuator/health`
5. **Monitor** - Open Grafana at port 3000
6. **Explore** - Try creating channels and sending messages

---

## 🎉 You're Done!

All services are running in Play with Docker. The application is fully functional and ready to test!

**Happy Testing!** 🚀
