
FROM maven:3.9-eclipse-temurin-21 as builder
WORKDIR /app
RUN mkdir -p /root/.m2
RUN echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"><repositories><repository><id>central</id><url>https://repo.maven.apache.org/maven2</url></repository></repositories></settings>' > /root/.m2/settings.xml
COPY pom.xml .
RUN mvn dependency:resolve -DskipTests
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
CMD ["java","-jar","app.jar"]
