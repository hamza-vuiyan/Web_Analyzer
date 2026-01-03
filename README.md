# Website Analyzer - Spring Boot

A Spring Boot application that analyzes websites for performance, security, and reliability metrics.

## Features

- **Performance Analysis**: Measures latency, compression, caching, and content size
- **Security Scoring**: Checks HTTPS, HSTS, CSP, and other security headers
- **Reliability Assessment**: Evaluates HTTP status codes, response headers, and caching

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Project Structure

```
website-analyzer/
├── src/
│   └── main/
│       ├── java/com/analyzer/
│       │   ├── WebsiteAnalyzerApplication.java  # Main Spring Boot application
│       │   ├── config/
│       │   │   ├── AnalyzerConfig.java          # Configuration constants
│       │   │   └── WebConfig.java               # Web/CORS configuration
│       │   ├── controller/
│       │   │   └── AnalyzerController.java      # REST API endpoints
│       │   ├── model/
│       │   │   ├── URLRequest.java              # Request DTO
│       │   │   ├── Result.java                  # Response DTO
│       │   │   ├── PerformanceDetails.java
│       │   │   ├── SecurityDetails.java
│       │   │   └── ReliabilityDetails.java
│       │   └── service/
│       │       ├── FetcherService.java          # HTTP client service
│       │       └── ScorerService.java           # Scoring logic
│       └── resources/
│           └── application.properties           # Application configuration
├── frontend/
│   ├── index.html
│   ├── app.js
│   └── style.css
└── pom.xml                                      # Maven configuration
```

## Building the Application

```bash
# Build with Maven
mvn clean package

# Or build without running tests
mvn clean package -DskipTests
```

## Running the Application

### Option 1: Using Maven
```bash
mvn spring-boot:run
```

### Option 2: Using the JAR file
```bash
java -jar target/website-analyzer-1.0.0.jar
```

The application will start on **http://localhost:8080**

## Using the Application

1. Open your browser and navigate to: http://localhost:8080
2. Enter one or more URLs (one per line) in the text area
3. Click "Analyze Websites"
4. View the ranked results with detailed metrics

## API Endpoint

### POST /analyze

Analyze multiple websites and return scoring results.

**Request Body:**
```json
{
  "urls": [
    "example.com",
    "https://google.com"
  ]
}
```

**Response:**
```json
[
  {
    "url": "https://example.com",
    "performance": 85,
    "security": 70,
    "reliability": 90,
    "total": 81,
    "backend": "Apache/2.4.41",
    "protocols": "HTTP/1.1 over TLS",
    "responseTime": "234 ms",
    "performanceDetails": { ... },
    "securityDetails": { ... },
    "reliabilityDetails": { ... }
  }
]
```

## Configuration

Edit [application.properties](src/main/resources/application.properties) to customize:

- Server port (default: 8080)
- HTTP timeout (default: 15000ms)
- Logging levels
- CORS settings

## Scoring Metrics

### Performance (0-100)
- **Latency**: ≤200ms (100 pts), ≤500ms (85 pts), ≤1000ms (70 pts), ≤2000ms (50 pts)
- **Compression**: +10 for gzip/brotli
- **Caching**: +10 for Cache-Control with max-age
- **Content Size**: +10 (≤300KB), +5 (≤1MB)

### Security (0-100)
- HTTPS: 30 points
- HSTS header: 20 points
- Content-Security-Policy: 20 points
- X-Content-Type-Options: 10 points
- X-Frame-Options: 10 points
- Referrer-Policy: 10 points

### Reliability (0-100)
- HTTP Status 2xx: +30 points
- HTTP Status 3xx: +15 points
- Response headers >20: +10 points
- Compression enabled: +5 points
- Caching enabled: +10 points

## Migration from Python

This Spring Boot version maintains feature parity with the original Python/FastAPI implementation:

- ✅ Same API endpoints and response format
- ✅ Same scoring algorithms
- ✅ Same frontend (HTML/CSS/JS)
- ✅ HTTPS/HTTP fallback logic
- ✅ Comprehensive error handling
- ✅ Detailed logging

### Key Differences:
- Port: 8080 (Spring Boot default) instead of 8000 (FastAPI default)
- HTTP Client: Apache HttpClient 5 instead of httpx
- Runtime: JVM instead of Python interpreter

## Development

### Running in Development Mode
```bash
mvn spring-boot:run
```

### Building for Production
```bash
mvn clean package
java -jar target/website-analyzer-1.0.0.jar
```

## Troubleshooting

**Port already in use:**
```bash
# Change port in application.properties
server.port=8081
```

**Java version issues:**
```bash
# Verify Java version
java -version  # Should be 17 or higher
```

**Maven not found:**
```bash
# Install Maven on Ubuntu/Debian
sudo apt-get install maven

# Install on macOS
brew install maven
```

## License

This project is open source and available for educational purposes.
