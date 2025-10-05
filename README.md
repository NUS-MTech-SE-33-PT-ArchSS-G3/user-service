# User Service

Microservice for managing user authentication, registration, profiles, and reputation in the BidderGod auction platform.

## Overview

The User Service is a Spring Boot application that provides core user management functionality including:
- User registration and authentication
- Profile management
- User reputation tracking
- OAuth2 integration with AWS Cognito

## Tech Stack

- **Java 21** with Spring Boot 3.5.5
- **Spring Security** with OAuth2 Resource Server
- **Spring Data JPA** with H2 database (development)
- **Maven** for dependency management
- **Docker** for containerization
- **AWS ECS** for deployment

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker (optional, for containerization)

### Local Development

1. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Run tests**
   ```bash
   ./mvnw test
   ```

3. **Build JAR**
   ```bash
   ./mvnw clean package
   ```

4. **Access the application**
   - API: http://localhost:8080
   - Health check: http://localhost:8080/actuator/health
   - API Documentation: http://localhost:8080/swagger-ui.html

### Docker

1. **Build image**
   ```bash
   docker build -t user-service .
   ```

2. **Run container**
   ```bash
   docker run -p 8080:8080 user-service
   ```

## API Documentation

API documentation is available via OpenAPI/Swagger:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI spec**: http://localhost:8080/v3/api-docs

## Configuration

Key configuration files:
- `src/main/resources/application.properties` - Application configuration
- `pom.xml` - Maven dependencies

## Deployment

The service is deployed to AWS ECS using GitHub Actions. The CI/CD pipeline:

1. **Build**: Compiles and packages the application
2. **Docker**: Builds and pushes image to AWS ECR
3. **Deploy**: Updates ECS service with new image

### Manual Deployment

Trigger deployment via GitHub Actions:
1. Go to Actions tab in GitHub
2. Select "Build and Push User Service to ECR"
3. Click "Run workflow"

### Environment Variables

Set these as GitHub organization secrets/variables:
- `AWS_ROLE_ARN` (secret) - IAM role for GitHub Actions
- `AWS_REGION` (variable) - AWS region
- `AWS_ECR_REPOSITORY` (variable) - ECR repository name
- `AWS_ECS_CLUSTER` (variable) - ECS cluster name

## Project Structure

```
user-service/
├── .github/
│   └── workflows/
│       └── user-service-ecr.yml    # CI/CD pipeline
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/biddergod/      # Application code
│   │   └── resources/
│   │       └── application.properties
│   └── test/                        # Test code
├── Dockerfile                       # Multi-stage Docker build
├── pom.xml                          # Maven configuration
└── README.md
```

## Health Checks

The application includes Spring Boot Actuator for health monitoring:
- Endpoint: `/actuator/health`
- ECS healthcheck: Configured in task definition

## Contributing

1. Create feature branch from `main`
2. Make changes and test locally
3. Push and create pull request
4. Wait for CI checks to pass
5. Merge to `main` to trigger deployment