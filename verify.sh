#!/bin/bash

# ========================================
# SecureCollab - Play with Docker Verification Script
# Repository: https://github.com/sushmitha0204/Week3/
# ========================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
PASSED=0
FAILED=0
TOTAL=0

# Helper functions
print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_test() {
    echo -e "${YELLOW}TEST: $1${NC}"
}

print_pass() {
    echo -e "${GREEN}✅ PASS: $1${NC}"
    ((PASSED++))
    ((TOTAL++))
}

print_fail() {
    echo -e "${RED}❌ FAIL: $1${NC}"
    ((FAILED++))
    ((TOTAL++))
}

print_info() {
    echo -e "${BLUE}ℹ️  INFO: $1${NC}"
}

# ========================================
# START VERIFICATION
# ========================================

print_header "SecureCollab - Docker Compose Verification"

# Test 1: Docker Compose Status
print_test "Docker Compose Services Running"
if docker-compose ps > /dev/null 2>&1; then
    if [ $(docker-compose ps -q | wc -l) -eq 7 ]; then
        print_pass "All 7 containers are running"
        docker-compose ps
    else
        RUNNING=$(docker-compose ps -q | wc -l)
        print_fail "Expected 7 containers, found $RUNNING"
        docker-compose ps
    fi
else
    print_fail "Docker Compose not responding"
fi

# Test 2: Gateway Service Health
print_test "Gateway Service Health Check (Port 8080)"
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null || echo "000")
if [ "$RESPONSE" = "200" ]; then
    print_pass "Gateway Service responding (HTTP $RESPONSE)"
else
    print_fail "Gateway Service not responding (HTTP $RESPONSE)"
fi

# Test 3: Chat Service Health
print_test "Chat Service Health Check (Port 8081)"
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/actuator/health 2>/dev/null || echo "000")
if [ "$RESPONSE" = "200" ]; then
    print_pass "Chat Service responding (HTTP $RESPONSE)"
else
    print_fail "Chat Service not responding (HTTP $RESPONSE)"
fi

# Test 4: WebSocket Service Health
print_test "WebSocket Service Health Check (Port 8082)"
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/actuator/health 2>/dev/null || echo "000")
if [ "$RESPONSE" = "200" ]; then
    print_pass "WebSocket Service responding (HTTP $RESPONSE)"
else
    print_fail "WebSocket Service not responding (HTTP $RESPONSE)"
fi

# Test 5: PostgreSQL Connection
print_test "PostgreSQL Database Connection (Port 5432)"
if docker-compose exec -T postgres pg_isready -U postgres > /dev/null 2>&1; then
    print_pass "PostgreSQL database is ready"
else
    print_fail "PostgreSQL database connection failed"
fi

# Test 6: Redis Connection
print_test "Redis Cache Connection (Port 6379)"
if docker-compose exec -T redis redis-cli PING > /dev/null 2>&1; then
    print_pass "Redis cache is ready"
else
    print_fail "Redis cache connection failed"
fi

# Test 7: Kafka Broker
print_test "Kafka Broker Connection (Port 9092)"
if docker-compose exec -T kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092 > /dev/null 2>&1; then
    print_pass "Kafka broker is ready"
else
    print_fail "Kafka broker connection failed"
fi

# Test 8: RabbitMQ Connection
print_test "RabbitMQ Message Broker (Port 5672)"
if docker-compose exec -T rabbitmq rabbitmq-diagnostics -q ping > /dev/null 2>&1; then
    print_pass "RabbitMQ is ready"
else
    print_fail "RabbitMQ connection failed"
fi

# Test 9: Prometheus Metrics
print_test "Prometheus Metrics Server (Port 9090)"
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9090/api/v1/targets 2>/dev/null || echo "000")
if [ "$RESPONSE" = "200" ]; then
    print_pass "Prometheus responding (HTTP $RESPONSE)"
else
    print_fail "Prometheus not responding (HTTP $RESPONSE)"
fi

# Test 10: Service Logs Check
print_test "Service Logs - Checking for errors"
if docker-compose logs | grep -i "error" | grep -v "WARNING" > /dev/null 2>&1; then
    ERROR_COUNT=$(docker-compose logs | grep -i "error" | grep -v "WARNING" | wc -l)
    print_fail "Found $ERROR_COUNT error messages in logs"
else
    print_pass "No critical errors found in logs"
fi

# Test 11: Gateway Service Logs
print_test "Gateway Service - Checking startup logs"
if docker-compose logs gateway-service | grep -i "started\|started on port\|initialized" > /dev/null 2>&1; then
    print_pass "Gateway Service started successfully"
else
    print_fail "Gateway Service startup status unclear"
fi

# Test 12: Chat Service Logs
print_test "Chat Service - Checking startup logs"
if docker-compose logs chat-service | grep -i "started\|started on port\|initialized" > /dev/null 2>&1; then
    print_pass "Chat Service started successfully"
else
    print_fail "Chat Service startup status unclear"
fi

# Test 13: WebSocket Service Logs
print_test "WebSocket Service - Checking startup logs"
if docker-compose logs websocket-service | grep -i "started\|started on port\|initialized" > /dev/null 2>&1; then
    print_pass "WebSocket Service started successfully"
else
    print_fail "WebSocket Service startup status unclear"
fi

# Test 14: Encryption Service Availability
print_test "Encryption Service - Checking initialization"
if docker-compose logs chat-service | grep -i "encryption\|crypto" > /dev/null 2>&1; then
    print_pass "Encryption Service initialized"
else
    print_info "Encryption Service status - check logs manually"
fi

# Test 15: Database Schema Check
print_test "Database Schema - Tables created"
if docker-compose exec -T postgres psql -U postgres -d securecollab -c "\dt" 2>/dev/null | grep -E "message|channel|user|key" > /dev/null; then
    print_pass "Database tables created successfully"
else
    print_fail "Database tables not found"
fi

# ========================================
# Resource Usage
# ========================================

print_header "Resource Usage Summary"

echo "Container Status:"
docker-compose ps

print_info "Memory Usage:"
docker stats --no-stream --format "table {{.Container}}\t{{.MemUsage}}" 2>/dev/null | head -10

print_info "CPU Usage:"
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}" 2>/dev/null | head -10

# ========================================
# API Endpoint Availability
# ========================================

print_header "API Endpoint Availability"

# Test each endpoint
ENDPOINTS=(
    "GET:8080:/actuator/health:Gateway Health"
    "GET:8081:/actuator/health:Chat Health"
    "GET:8082:/actuator/health:WebSocket Health"
    "POST:8080:/api/chat/login:Authentication"
    "GET:8081:/api/chat/channels:Channels List"
)

for endpoint in "${ENDPOINTS[@]}"; do
    IFS=':' read -r METHOD PORT PATH DESC <<< "$endpoint"
    print_test "$DESC ($METHOD $PORT$PATH)"
    
    if [ "$METHOD" = "GET" ]; then
        RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost$PORT$PATH 2>/dev/null || echo "000")
    else
        RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X $METHOD http://localhost$PORT$PATH \
          -H "Content-Type: application/json" \
          -d '{}' 2>/dev/null || echo "000")
    fi
    
    if [ "$RESPONSE" = "200" ] || [ "$RESPONSE" = "201" ] || [ "$RESPONSE" = "401" ]; then
        print_pass "$DESC - HTTP $RESPONSE"
    else
        print_fail "$DESC - HTTP $RESPONSE"
    fi
done

# ========================================
# Summary
# ========================================

print_header "Verification Summary"

echo -e "Total Tests Run: ${BLUE}$TOTAL${NC}"
echo -e "Passed: ${GREEN}$PASSED${NC}"
echo -e "Failed: ${RED}$FAILED${NC}"

SUCCESS_RATE=$((PASSED * 100 / TOTAL))
echo -e "Success Rate: ${BLUE}$SUCCESS_RATE%${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}✅ ALL TESTS PASSED - SYSTEM IS READY!${NC}"
    exit 0
else
    echo -e "\n${RED}⚠️  SOME TESTS FAILED - CHECK LOGS${NC}"
    echo -e "${YELLOW}Run 'docker-compose logs' for more details${NC}"
    exit 1
fi
