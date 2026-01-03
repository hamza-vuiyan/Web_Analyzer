#!/bin/bash

# Website Analyzer - Spring Boot Startup Script

echo "=========================================="
echo "  Website Analyzer - Spring Boot"
echo "=========================================="
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java is not installed"
    echo "Please install Java 17 or higher"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Error: Java 17 or higher is required"
    echo "Current version: $(java -version 2>&1 | head -n 1)"
    exit 1
fi

echo "✓ Java version: $(java -version 2>&1 | head -n 1)"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven is not installed"
    echo "Please install Maven 3.6 or higher"
    exit 1
fi

echo "✓ Maven version: $(mvn -version | head -n 1)"
echo ""

# Build the project
echo "📦 Building the project..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo ""
echo "✓ Build successful"
echo ""

# Start the application
echo "🚀 Starting Website Analyzer on http://localhost:8080"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

java -jar target/website-analyzer-1.0.0.jar
