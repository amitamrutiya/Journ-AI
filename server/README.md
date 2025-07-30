# JournAI Java Spring Boot Server

This is the Java Spring Boot version of the JournAI server, migrated from the original Node.js/TypeScript implementation.

## Features

- **Authentication**: Clerk-based authentication with JWT token validation
- **Journal Management**: CRUD operations for journal entries
- **AI Analysis**: Gemini AI integration for journal mood analysis
- **Database**: PostgreSQL with JPA/Hibernate
- **Rate Limiting**: Request rate limiting for API endpoints
- **CORS**: Configurable CORS support
- **Health Checks**: Health monitoring endpoints
- **Error Handling**: Global exception handling
- **Validation**: Request validation with Bean Validation

## API Endpoints

### Health & Status
- `GET /` - Root endpoint with service status
- `GET /health` - Health check endpoint

### Authentication
- `GET /protected` - Get authenticated user data
- `GET /api/user-journals` - Get user with journal IDs

### Journal Operations
- `POST /api/analyze-journal` - Analyze journal text with AI (supports anonymous users)
- `POST /api/save-journal` - Save a journal entry (requires authentication)
- `PUT /api/update-journal/{id}` - Update an existing journal entry
- `GET /api/get-user-journal` - Get user's journal entries (with optional month filter)
- `GET /api/journal/{id}` - Get specific journal by ID
- `DELETE /api/delete-journal/{id}` - Delete a journal entry
- `GET /api/journals/insights` - Get journal analytics and insights

### Webhooks
- `POST /webhooks/clerk` - Clerk webhook handler for user events

## Technology Stack

- **Java 21**
- **Spring Boot 3.5.4**
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database abstraction
- **PostgreSQL** - Database
- **Jackson** - JSON processing
- **JWT** - Token validation
- **WebFlux** - HTTP client for external APIs
- **Bucket4j** - Rate limiting
- **Maven** - Build tool

## Environment Configuration

Copy `.env.example` to `.env` and configure the following variables:

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/journai_db

# Clerk Authentication
CLERK_SECRET_KEY=your_clerk_secret_key_here
CLERK_WEBHOOK_SECRET=your_clerk_webhook_secret_here

# Gemini AI
GEMINI_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-1.5-flash

# Server
PORT=8000
SPRING_PROFILES_ACTIVE=development

# CORS
CORS_ORIGIN=http://localhost:3000
```

## Running the Application

### Prerequisites
- Java 21 or higher
- PostgreSQL database
- Clerk account and API keys
- Google Gemini API key

### Development Mode
```bash
chmod +x start.sh
./start.sh
```

Or manually:
```bash
./mvnw spring-boot:run
```

### Production Build
```bash
./mvnw clean package
java -jar target/server-0.0.1-SNAPSHOT.jar
```

### Docker
```bash
docker build -t journai-server .
docker run -p 8000:8000 --env-file .env journai-server
```

## Database Schema

The application uses JPA entities that automatically create the following tables:

### Users Table
- `id` (String, Primary Key)
- `email` (String, Unique)
- `name` (String, Optional)
- `image_url` (String, Optional)
- `created_at` (Timestamp)
- `updated_at` (Timestamp)

### Journals Table
- `id` (String, Primary Key, UUID)
- `user_id` (String, Foreign Key)
- `title` (String)
- `content` (JSONB)
- `mood` (Enum)
- `summary` (Text)
- `created_at` (Timestamp)
- `updated_at` (Timestamp)

### Mood Enum Values
- HAPPY, SAD, ANXIOUS, NEUTRAL, EXCITED, ANGRY, PEACEFUL, GRATEFUL, FRUSTRATED, WORRIED, CONTENT, LONELY, OVERWHELMED, HOPEFUL, BORED, TIRED

## Security

- JWT token validation for protected endpoints
- CORS configuration for cross-origin requests
- Rate limiting on analysis endpoints
- Input validation on all requests
- SQL injection prevention through JPA
- Global exception handling

## Migration from Node.js

This Java implementation maintains full compatibility with the original Node.js API:

✅ **Identical API Endpoints**: All endpoints have the same paths and behavior  
✅ **Same Request/Response Format**: JSON structures match exactly  
✅ **Database Schema**: Compatible with existing Prisma schema  
✅ **Authentication**: Clerk integration works identically  
✅ **AI Integration**: Gemini API calls maintained  
✅ **Rate Limiting**: Analysis endpoint rate limiting preserved  
✅ **Error Handling**: Similar error responses and status codes  

## Logging

The application uses SLF4J with Logback for structured logging:
- Request/response logging for API calls
- Error logging with stack traces
- Performance metrics logging
- Security event logging

## Monitoring

Health check endpoint provides:
- Service status
- Uptime information
- Environment details
- Database connectivity status (via Spring Actuator)

## Development

### Code Structure
```
src/main/java/com/journai/server/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/             # Data Transfer Objects
├── exception/       # Exception handling
├── model/           # JPA entities
├── repository/      # Data repositories
├── security/        # Authentication & security
└── service/         # Business logic
```

### Testing
```bash
./mvnw test
```

### Building
```bash
./mvnw clean package
```

## Deployment

The application can be deployed using:
- JAR file deployment
- Docker containers
- Kubernetes (using existing k8s manifests)
- Cloud platforms (AWS, GCP, Azure)

## Performance

- Connection pooling with HikariCP
- JPA query optimization
- Async processing for external API calls
- Rate limiting to prevent abuse
- Efficient JSON serialization

## Troubleshooting

### Common Issues
1. **Database Connection**: Ensure PostgreSQL is running and URL is correct
2. **Clerk Authentication**: Verify secret keys are properly configured
3. **Gemini API**: Check API key and quota limits
4. **CORS**: Ensure frontend URL is in CORS_ORIGIN

### Logs
Check application logs for detailed error information:
```bash
tail -f logs/application.log
```
