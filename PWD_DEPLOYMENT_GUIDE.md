# SecureCollab Deployment Guide for Play with Docker (PWD)

## Prerequisites
- Docker account (free at https://hub.docker.com)
- Play with Docker access (https://labs.play-with-docker.com)

## Step 1: Push Docker Images to Docker Hub

### 1.1 Build Docker Images Locally
```bash
cd c:\Users\g.sai.sushmitha\week3pip

# Build all services
docker-compose build
```

### 1.2 Tag Images for Docker Hub
Replace `YOUR_DOCKERHUB_USERNAME` with your Docker Hub username:

```bash
# Chat Service
docker tag week3pip-chat-service:latest YOUR_DOCKERHUB_USERNAME/securecolllab-chat-service:latest

# WebSocket Service
docker tag week3pip-websocket-service:latest YOUR_DOCKERHUB_USERNAME/securecolllab-websocket-service:latest

# Gateway Service
docker tag week3pip-gateway-service:latest YOUR_DOCKERHUB_USERNAME/securecolllab-gateway-service:latest
```

### 1.3 Push to Docker Hub
```bash
# Login to Docker Hub
docker login

# Push images
docker push YOUR_DOCKERHUB_USERNAME/securecolllab-chat-service:latest
docker push YOUR_DOCKERHUB_USERNAME/securecolllab-websocket-service:latest
docker push YOUR_DOCKERHUB_USERNAME/securecolllab-gateway-service:latest
```

## Step 2: Access Play with Docker

1. Go to https://labs.play-with-docker.com
2. Click "Login with Docker"
3. Sign in with your Docker account
4. Click "+ ADD NEW INSTANCE" to start a new session

## Step 3: Deploy on PWD

### 3.1 In the PWD terminal, clone the repository:
```bash
git clone https://github.com/YOUR_GITHUB_USERNAME/week3pip.git
cd week3pip
```

### 3.2 Update docker-compose.yml
Edit the image references to use your Docker Hub images:

```bash
sed -i 's/week3pip-chat-service/YOUR_DOCKERHUB_USERNAME\/securecolllab-chat-service/g' docker-compose.yml
sed -i 's/week3pip-websocket-service/YOUR_DOCKERHUB_USERNAME\/securecolllab-websocket-service/g' docker-compose.yml
sed -i 's/week3pip-gateway-service/YOUR_DOCKERHUB_USERNAME\/securecolllab-gateway-service/g' docker-compose.yml
```

### 3.3 Start the Application
```bash
# Deploy using docker-compose
docker-compose up -d

# Wait for services to start (about 30 seconds)
sleep 30

# Check service status
docker-compose ps
```

## Step 4: Verify Services

### 4.1 Check Logs
```bash
# Chat Service logs
docker-compose logs chat-service

# WebSocket Service logs
docker-compose logs websocket-service

# Gateway Service logs
docker-compose logs gateway-service
```

### 4.2 Test API Endpoints

#### Chat Service (Port 8081)
```bash
# Health check
curl http://localhost:8081/actuator/health

# Send a message
curl -X POST http://localhost:8081/api/chat/send \
  -H "Content-Type: application/json" \
  -d '{
    "channelId": "general",
    "senderId": "user1",
    "senderName": "User One",
    "content": "Hello SecureCollab!",
    "isEncrypted": true
  }'
```

#### WebSocket Service (Port 8082)
```bash
# Health check
curl http://localhost:8082/actuator/health

# WebSocket endpoint: ws://localhost:8082/ws/chat/user1
```

#### Gateway Service (Port 8080)
```bash
# Health check
curl http://localhost:8080/actuator/health

# Route to chat service
curl http://localhost:8080/chat/api/chat/channels

# Route to websocket service
# Access frontend: http://localhost:8080/
```

### 4.3 Frontend Verification
1. Open browser to `http://localhost:8080`
2. Should display the SecureCollab chat interface
3. Create/join channels and test messaging

## Step 5: Test Features

### 5.1 End-to-End Encryption
- Send encrypted messages
- Verify decryption on recipient side
- Check encryption keys are rotated correctly

### 5.2 Offline Message Queue
- Take a service offline temporarily
- Send messages to offline user
- Bring service back online
- Verify queued messages are delivered

### 5.3 Toxicity Detection
- Send a message with profanity
- Verify the system flags/filters it
- Check logs for detection

### 5.4 Voice Call Service
- Initiate a voice call
- Verify call establishment
- Check call duration tracking

### 5.5 File Sharing
- Upload a file
- Download from another client
- Verify file integrity

## Step 6: Monitor Performance

### 6.1 Check Resource Usage
```bash
docker stats
```

### 6.2 View Application Metrics
```bash
# Prometheus metrics (if configured)
curl http://localhost:8081/actuator/prometheus
curl http://localhost:8082/actuator/prometheus
curl http://localhost:8080/actuator/prometheus
```

### 6.3 Database Health
```bash
# Check PostgreSQL connectivity
docker-compose exec postgres psql -U securecollab -d securecollab -c "SELECT 1;"
```

## Step 7: Cleanup

### 7.1 Stop Services
```bash
docker-compose down
```

### 7.2 Remove All Resources
```bash
docker-compose down -v  # Includes volumes
```

## Troubleshooting

### Services Not Starting
```bash
# Check logs for specific service
docker-compose logs SERVICE_NAME --tail=100

# Restart services
docker-compose restart

# Full rebuild
docker-compose down -v
docker-compose up -d --build
```

### Network Connectivity Issues
```bash
# Check network
docker network ls
docker network inspect week3pip_default

# Test connectivity between containers
docker-compose exec chat-service ping websocket-service
docker-compose exec websocket-service ping chat-service
```

### Database Issues
```bash
# Check PostgreSQL
docker-compose exec postgres psql -U securecollab -d securecollab -l

# View PostgreSQL logs
docker-compose logs postgres
```

### Port Conflicts
If ports 8080, 8081, 8082 are in use, update `docker-compose.yml`:
```yaml
services:
  chat-service:
    ports:
      - "9081:8081"  # Changed from 8081
  websocket-service:
    ports:
      - "9082:8082"  # Changed from 8082
  gateway-service:
    ports:
      - "9080:8080"  # Changed from 8080
```

## Architecture Verification Checklist

- [ ] Chat Service running on port 8081
- [ ] WebSocket Service running on port 8082
- [ ] Gateway Service running on port 8080
- [ ] PostgreSQL connected and accessible
- [ ] RabbitMQ running for message queuing
- [ ] Kafka running for event streaming
- [ ] All microservices communicating successfully
- [ ] Frontend loads without errors
- [ ] API endpoints returning expected responses
- [ ] WebSocket connections establishing
- [ ] Encryption working end-to-end
- [ ] Offline message queue functioning
- [ ] Authentication/Authorization working

## Performance Baseline

After successful deployment, note:
- Response times for API calls
- WebSocket latency
- Message encryption/decryption performance
- System resource usage (CPU, Memory, Disk)
- Database query performance

## Next Steps

1. Load testing with multiple concurrent users
2. Security audit of API endpoints
3. Database backup and recovery testing
4. Horizontal scaling testing
5. Failover and recovery procedures
6. Integration testing with external services
