# IDATT2105_Fullstack_Project
Extensive project for fullstack covering both backend and frontend

## Getting started

Run `docker compose up -d --build` to run everything
Run `docker compose down` to take down the app.


Run `docker compose down --volumes` to remove all volumes (stored data).

Visit `http://localhost:8080/api/health` to make sure the website works.


## Useful commands

### Having problems where schema isn't reset?
1. `docker compose down -v` to wipe db volume
2. `docker compose up -d --force-recreate db` to recreate db
3. Reapply schema using the `schema.sql` file

## Unimportant stuff

Backend setup command
```
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=4.0.5 \
  -d baseDir=backend \
  -d groupId=com.grimni \
  -d artifactId=backend \
  -d name=backend \
  -d packageName=com.grimni.backend \
  -d packaging=jar \
  -d javaVersion=21 \
  -d dependencies=web \
  -o backend.zip
```
