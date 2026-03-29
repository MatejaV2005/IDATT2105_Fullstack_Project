# IDATT2105_Fullstack_Project
Extensive project for fullstack covering both backend and frontend

## Getting started

Run `docker compose up -d --build` to run everything

Visit `http://localhost:8080/api/health` to make sure the website works.

## Unimportant stuff

Backend setup command
```
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=4.0.5 \
  -d baseDir=backend \
  -d groupId=com.example \
  -d artifactId=backend \
  -d name=backend \
  -d packageName=com.grimni.backend \
  -d packaging=jar \
  -d javaVersion=21 \
  -d dependencies=web \
  -o backend.zip
```
