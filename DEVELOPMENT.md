# Local Development Guide
*Updated: October 6, 2025*

## Prerequisites
- **Java 17 LTS** installed and JAVA_HOME configured
- **Git** for version control
- **Maven 3.9.9+** (optional - project includes Maven wrapper)
- **Docker Desktop** (optional - for containerized development)

## Development Environment Setup

### Build locally (Windows PowerShell):

```powershell
cd E:\Desktop\GitHub\Flight-Advisor
# Ensure JAVA_HOME points to your JDK 17 installation
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
.\mvnw.cmd clean compile
```

### Build locally (Unix/Linux/macOS):

```bash
cd /path/to/Flight-Advisor
# Ensure JAVA_HOME points to your JDK 17 installation
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
./mvnw clean compile
```

If you see `Error: JAVA_HOME not found in your environment`, set JAVA_HOME and retry.

## Modern Development Workflows

### Quick Start Commands:
```bash
# Clean build and run tests
./mvnw clean test

# Build and package (skip tests for speed)
./mvnw clean package -DskipTests

# Run the application locally
./mvnw spring-boot:run

# Run with production profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### Docker Development:
```bash
# Build and run with Docker Compose
docker-compose up --build

# Run in detached mode
docker-compose up -d

# View logs
docker-compose logs -f flight-advisor
```

## Continuous Integration
The project includes modern GitHub Actions workflows:
- `.github/workflows/ci-tests.yml` — runs unit tests on GitHub Actions (Ubuntu / JDK 17)
- `.github/workflows/ci-build.yml` — builds and creates Docker image
- `.github/workflows/maven-ci.yml` — basic Maven build workflow

All workflows can be triggered manually from the Actions tab (workflow_dispatch).

## IDE Configuration
For optimal development experience:
- **IntelliJ IDEA**: Import as Maven project, enable annotation processing
- **VS Code**: Install Java Extension Pack, Spring Boot Extension Pack
- **Eclipse/STS**: Import as existing Maven project

## Hot Reload Development
Enable Spring Boot DevTools for hot reload during development:
```bash
./mvnw spring-boot:run -Dspring-boot.run.fork=false
```
