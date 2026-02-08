# Local Build Guide - SecureCollab

## Step 1: Install Java 21

### Download
1. Go to: https://www.oracle.com/java/technologies/downloads/#java21
2. Download: **Windows x64 Installer**
3. Run the installer
4. Accept defaults and install

### Verify
```powershell
java -version
```
Should show: `java version "21.x.x"`

---

## Step 2: Install Maven 3.9

### Download
1. Go to: https://maven.apache.org/download.cgi
2. Download: **apache-maven-3.9.x-bin.zip** (Binary archive)
3. Extract to: `C:\maven` or `C:\Program Files\maven`

### Add to PATH
1. Right-click **This PC** or **My Computer** → Properties
2. Click **Advanced system settings**
3. Click **Environment Variables**
4. Under **System variables**, click **New**
   - Variable name: `MAVEN_HOME`
   - Variable value: `C:\maven` (or your extraction path)
5. Find `Path` in System variables, click **Edit**
6. Click **New** and add: `%MAVEN_HOME%\bin`
7. Click **OK** three times
8. **Restart PowerShell**

### Verify (in new PowerShell)
```powershell
mvn -version
```
Should show Maven 3.9.x and Java 21

---

## Step 3: Build Services

```powershell
cd c:\Users\g.sai.sushmitha\week3pip

# Build chat-service
cd chat-service
mvn clean package -DskipTests
cd ..

# Build gateway-service
cd gateway-service
mvn clean package -DskipTests
cd ..

# Build websocket-service
cd websocket-service
mvn clean package -DskipTests
cd ..

# JAR files will be in: service/target/*.jar
```

---

## Step 4: Verify Build Success

After each build, you should see:
```
[INFO] BUILD SUCCESS
[INFO] Total time: X.XXs
```

JAR files created:
- `chat-service/target/chat-service-1.0.0.jar`
- `gateway-service/target/gateway-service-1.0.0.jar`
- `websocket-service/target/websocket-service-1.0.0.jar`

---

## Troubleshooting

### Java not found after install
- Restart PowerShell completely
- Run: `java -version`

### Maven not found after PATH update
- Restart PowerShell
- Run: `mvn -version`

### Build fails with "Cannot find symbol"
- Make sure Java 21 is installed (not Java 17)
- Run: `java -version`

### Permission denied errors
- Run PowerShell as Administrator
- Or right-click PowerShell → "Run as administrator"

---

## Total Time
- Java 21 install: ~5 min
- Maven install: ~3 min
- First build: ~3-5 min (downloads dependencies)
- Subsequent builds: ~1-2 min

**Total: ~15-20 minutes for first build**

---

## After Build - Run Services

Once JAR files are built, you can run them:

```powershell
# Terminal 1 - Chat Service
java -jar chat-service/target/chat-service-1.0.0.jar

# Terminal 2 - Gateway Service
java -jar gateway-service/target/gateway-service-1.0.0.jar

# Terminal 3 - WebSocket Service
java -jar websocket-service/target/websocket-service-1.0.0.jar
```

But you'll also need:
- PostgreSQL 15
- Redis 7
- Kafka + RabbitMQ
- Prometheus + Grafana

**For full stack, Docker Compose is easier!**
