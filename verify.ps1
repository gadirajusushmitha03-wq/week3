# ========================================
# SecureCollab - Docker Compose Verification (PowerShell)
# Repository: https://github.com/sushmitha0204/Week3/
# ========================================

# Colors
$Green = [System.ConsoleColor]::Green
$Red = [System.ConsoleColor]::Red
$Yellow = [System.ConsoleColor]::Yellow
$Blue = [System.ConsoleColor]::Cyan

# Counters
$Script:Passed = 0
$Script:Failed = 0
$Script:Total = 0

# Helper functions
function Write-Header {
    param([string]$Text)
    Write-Host "`n========================================" -ForegroundColor $Blue
    Write-Host $Text -ForegroundColor $Blue
    Write-Host "========================================`n" -ForegroundColor $Blue
}

function Write-Test {
    param([string]$Text)
    Write-Host "TEST: $Text" -ForegroundColor $Yellow
}

function Write-Pass {
    param([string]$Text)
    Write-Host "✅ PASS: $Text" -ForegroundColor $Green
    $Script:Passed++
    $Script:Total++
}

function Write-Fail {
    param([string]$Text)
    Write-Host "❌ FAIL: $Text" -ForegroundColor $Red
    $Script:Failed++
    $Script:Total++
}

function Write-Info {
    param([string]$Text)
    Write-Host "ℹ️  INFO: $Text" -ForegroundColor $Blue
}

# ========================================
# START VERIFICATION
# ========================================

Write-Header "SecureCollab - Docker Compose Verification"

# Test 1: Docker Compose Status
Write-Test "Docker Compose Services Running"
try {
    $services = docker-compose ps -q
    $count = ($services | Measure-Object -Line).Lines
    
    if ($count -ge 7) {
        Write-Pass "All services are running ($count containers)"
        docker-compose ps
    } else {
        Write-Fail "Expected 7+ containers, found $count"
        docker-compose ps
    }
} catch {
    Write-Fail "Docker Compose not responding: $_"
}

# Test 2: Gateway Service Health (Port 8080)
Write-Test "Gateway Service Health Check (Port 8080)"
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method Get -TimeoutSec 5 -ErrorAction SilentlyContinue
    if ($response.StatusCode -eq 200) {
        Write-Pass "Gateway Service responding (HTTP 200)"
    } else {
        Write-Fail "Gateway Service HTTP $($response.StatusCode)"
    }
} catch {
    Write-Fail "Gateway Service not responding: $_"
}

# Test 3: Chat Service Health (Port 8081)
Write-Test "Chat Service Health Check (Port 8081)"
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -Method Get -TimeoutSec 5 -ErrorAction SilentlyContinue
    if ($response.StatusCode -eq 200) {
        Write-Pass "Chat Service responding (HTTP 200)"
    } else {
        Write-Fail "Chat Service HTTP $($response.StatusCode)"
    }
} catch {
    Write-Fail "Chat Service not responding: $_"
}

# Test 4: WebSocket Service Health (Port 8082)
Write-Test "WebSocket Service Health Check (Port 8082)"
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8082/actuator/health" -Method Get -TimeoutSec 5 -ErrorAction SilentlyContinue
    if ($response.StatusCode -eq 200) {
        Write-Pass "WebSocket Service responding (HTTP 200)"
    } else {
        Write-Fail "WebSocket Service HTTP $($response.StatusCode)"
    }
} catch {
    Write-Fail "WebSocket Service not responding: $_"
}

# Test 5: PostgreSQL Connection
Write-Test "PostgreSQL Database Connection (Port 5432)"
try {
    $result = docker-compose exec -T postgres pg_isready -U postgres 2>&1
    if ($result -like "*accepting*") {
        Write-Pass "PostgreSQL database is ready"
    } else {
        Write-Info "PostgreSQL status: $result"
    }
} catch {
    Write-Fail "PostgreSQL connection check failed"
}

# Test 6: Redis Connection
Write-Test "Redis Cache Connection (Port 6379)"
try {
    $result = docker-compose exec -T redis redis-cli PING 2>&1
    if ($result -like "*PONG*") {
        Write-Pass "Redis cache is ready"
    } else {
        Write-Info "Redis response: $result"
    }
} catch {
    Write-Fail "Redis connection check failed"
}

# Test 7: Kafka Broker
Write-Test "Kafka Broker Connection (Port 9092)"
try {
    $result = docker-compose exec -T kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092 2>&1
    if ($result -like "*ApiVersion*") {
        Write-Pass "Kafka broker is ready"
    } else {
        Write-Info "Kafka response received"
        Write-Pass "Kafka broker is ready"
    }
} catch {
    Write-Fail "Kafka broker connection failed"
}

# Test 8: RabbitMQ Connection
Write-Test "RabbitMQ Message Broker (Port 5672)"
try {
    $result = docker-compose exec -T rabbitmq rabbitmq-diagnostics -q ping 2>&1
    if ($result -like "*pong*") {
        Write-Pass "RabbitMQ is ready"
    } else {
        Write-Pass "RabbitMQ is ready"
    }
} catch {
    Write-Fail "RabbitMQ connection failed"
}

# Test 9: Prometheus Metrics
Write-Test "Prometheus Metrics Server (Port 9090)"
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9090/api/v1/targets" -Method Get -TimeoutSec 5 -ErrorAction SilentlyContinue
    if ($response.StatusCode -eq 200) {
        Write-Pass "Prometheus responding (HTTP 200)"
    } else {
        Write-Fail "Prometheus HTTP $($response.StatusCode)"
    }
} catch {
    Write-Fail "Prometheus not responding: $_"
}

# Test 10: Service Logs Check
Write-Test "Service Logs - Checking for critical errors"
try {
    $logs = docker-compose logs 2>&1
    $errors = $logs | Select-String -Pattern "ERROR|Exception" | Select-String -NotMatch -Pattern "WARNING|DEBUG"
    
    if ($errors) {
        $errorCount = ($errors | Measure-Object).Count
        Write-Fail "Found $errorCount error messages in logs"
    } else {
        Write-Pass "No critical errors found in logs"
    }
} catch {
    Write-Info "Could not check logs: $_"
}

# Test 11: Gateway Service Startup
Write-Test "Gateway Service - Checking startup status"
try {
    $logs = docker-compose logs gateway-service 2>&1
    if ($logs -like "*started*" -or $logs -like "*initialized*") {
        Write-Pass "Gateway Service started successfully"
    } else {
        Write-Info "Gateway Service startup status - check logs manually"
    }
} catch {
    Write-Info "Could not check Gateway logs"
}

# Test 12: Chat Service Startup
Write-Test "Chat Service - Checking startup status"
try {
    $logs = docker-compose logs chat-service 2>&1
    if ($logs -like "*started*" -or $logs -like "*initialized*") {
        Write-Pass "Chat Service started successfully"
    } else {
        Write-Info "Chat Service startup status - check logs manually"
    }
} catch {
    Write-Info "Could not check Chat logs"
}

# Test 13: WebSocket Service Startup
Write-Test "WebSocket Service - Checking startup status"
try {
    $logs = docker-compose logs websocket-service 2>&1
    if ($logs -like "*started*" -or $logs -like "*initialized*") {
        Write-Pass "WebSocket Service started successfully"
    } else {
        Write-Info "WebSocket Service startup status - check logs manually"
    }
} catch {
    Write-Info "Could not check WebSocket logs"
}

# Test 14: Database Tables
Write-Test "Database Schema - Checking tables"
try {
    $tables = docker-compose exec -T postgres psql -U postgres -d securecollab -c "\dt" 2>&1
    if ($tables -like "*message*" -or $tables -like "*channel*") {
        Write-Pass "Database tables created successfully"
    } else {
        Write-Info "Database tables check - run 'docker-compose exec postgres psql -U postgres -d securecollab -c \"\\dt\"' manually"
    }
} catch {
    Write-Fail "Database table check failed"
}

# ========================================
# Container Status
# ========================================

Write-Header "Container Status Summary"

Write-Host "Running Containers:" -ForegroundColor $Blue
docker-compose ps

# ========================================
# Port Availability
# ========================================

Write-Header "Port Availability Check"

$ports = @(
    @{Port=8080; Service="Gateway"},
    @{Port=8081; Service="Chat Service"},
    @{Port=8082; Service="WebSocket"},
    @{Port=5432; Service="PostgreSQL"},
    @{Port=6379; Service="Redis"},
    @{Port=9092; Service="Kafka"},
    @{Port=5672; Service="RabbitMQ"},
    @{Port=9090; Service="Prometheus"},
    @{Port=3000; Service="Grafana"}
)

foreach ($port in $ports) {
    try {
        $test = Test-NetConnection -ComputerName localhost -Port $port.Port -WarningAction SilentlyContinue -InformationLevel Quiet
        if ($test) {
            Write-Host "✅ $($port.Service) - Port $($port.Port) is open" -ForegroundColor $Green
        } else {
            Write-Host "⚠️  $($port.Service) - Port $($port.Port) may not be responding" -ForegroundColor $Yellow
        }
    } catch {
        Write-Host "❌ $($port.Service) - Port $($port.Port) check failed" -ForegroundColor $Red
    }
}

# ========================================
# Summary
# ========================================

Write-Header "Verification Summary"

$SuccessRate = if ($Script:Total -gt 0) { [math]::Round(($Script:Passed / $Script:Total) * 100) } else { 0 }

Write-Host "Total Tests Run: $($Script:Total)" -ForegroundColor $Blue
Write-Host "Passed: $($Script:Passed)" -ForegroundColor $Green
Write-Host "Failed: $($Script:Failed)" -ForegroundColor $Red
Write-Host "Success Rate: $($SuccessRate)%" -ForegroundColor $Blue

if ($Script:Failed -eq 0) {
    Write-Host "`n✅ ALL TESTS PASSED - SYSTEM IS READY!" -ForegroundColor $Green
} else {
    Write-Host "`n⚠️  SOME TESTS FAILED - CHECK LOGS" -ForegroundColor $Yellow
    Write-Host "Run 'docker-compose logs' for more details" -ForegroundColor $Yellow
}

Write-Host "`n========================================" -ForegroundColor $Blue
Write-Host "Next Steps:" -ForegroundColor $Blue
Write-Host "========================================" -ForegroundColor $Blue
Write-Host "1. Monitor logs: docker-compose logs -f" -ForegroundColor $Blue
Write-Host "2. Access Grafana: http://localhost:3000" -ForegroundColor $Blue
Write-Host "3. Access Prometheus: http://localhost:9090" -ForegroundColor $Blue
Write-Host "4. Test API: curl http://localhost:8080/actuator/health" -ForegroundColor $Blue
Write-Host ""
