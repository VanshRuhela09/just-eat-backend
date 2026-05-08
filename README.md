# JustEat Backend

This is the backend service for the JustEat food ordering platform, built with Spring Boot. It provides RESTful APIs for authentication, user management, restaurant and menu operations, cart, and order processing.

## Features
- User authentication and authorization (Spring Security, JWT)
- Role-based access control (Admin, Owner, Customer)
- Restaurant and menu management
- Cart and order management
- Secure CORS configuration
- Environment variable support for cloud and container deployments
- Optimized database queries and caching
- Docker and Docker Compose support
- Swagger/OpenAPI documentation
- Unit tests with JUnit and Mockito

## Getting Started

### Prerequisites
- Java 21
- Maven
- PostgreSQL (or use Docker Compose)

### Build and Run (Local)
1. Clone the repository.
2. Configure your database in `src/main/resources/application.properties` or via environment variables.
3. Build the project:
   ```
   ./mvnw clean package
   ```
4. Run the application:
   ```
   java -jar target/app.jar
   ```

### Run with Docker Compose
1. Ensure Docker is installed.
2. Build and start services:
   ```
   docker-compose up --build
   ```
3. The backend will be available at `http://localhost:8081`.

## API Documentation
- Swagger UI: [http://localhost:8081/api/swagger-ui.html](http://localhost:8081/api/swagger-ui.html)

## Configuration
All sensitive and environment-dependent values can be set via environment variables or `.env` file. See `application.properties` for details.

## Testing
Run all unit tests:
```
./mvnw test
```

## Project Structure
- `src/main/java/com/justeat/backend/` - Main backend source code
- `src/test/java/com/justeat/backend/` - Unit tests
- `docker-compose.yml` - Multi-container orchestration
- `Dockerfile` - Container build
- `aiusage.md` - AI usage documentation

## License
This project is for educational and demonstration purposes.

