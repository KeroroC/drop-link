#!/bin/bash
set -e

echo "=== Building Frontend ==="
cd frontend
npm install
npm run build
cd ..

echo "=== Copying Frontend to Backend Static ==="
mkdir -p backend/src/main/resources/static
rm -rf backend/src/main/resources/static/*
cp -r frontend/dist/* backend/src/main/resources/static/

echo "=== Building Backend JAR ==="
cd backend
./mvnw clean package -DskipTests
cd ..

echo "=== Build Complete ==="
echo "JAR location: backend/target/drop-link-0.0.1-SNAPSHOT.jar"
echo ""
echo "To run:"
echo "  java -jar backend/target/drop-link-0.0.1-SNAPSHOT.jar"
