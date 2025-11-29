
Lancer le projet :

```bash
docker compose up --build -d
```

Accéder à l'application :

Une fois lancé, tout se passe sur le port 80 (Nginx) :

- Application Web : http://localhost/

- API Backend : http://localhost/api/

- PhpMyAdmin : http://localhost:8080/

## Dockerisation de projet

Preparer les Dockerfile, une pour le backend et l'autre pour le front

Dans le dossier de backend, on ajoute le fichier `Dockerfile`
```Dockerfile
FROM gradle:8.5-jdk17 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN gradle bootJar -x test --no-daemon

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Egalement, ajouter le `Dockerfile` dans le dossier de frontend `../Front`:

```Dockerfile
# On utilise une image Python ultra-légère (Alpine)
FROM python:3.11-alpine

WORKDIR /app
COPY . .

EXPOSE 8000

CMD ["python3", "-m", "http.server", "8000"]
```

Modifier l'ancien `docker-compose` pour prendre en compte la dockeristaion :

```yaml
version: '3.8'

services:
  # 1. Base de données
  mysql:
    image: mysql:8
    container_name: mysql8.0
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: mydb
    ports:
      - "3307:3306"
    volumes:
      - mysql_data:/var/lib/mysql
        
  phpmyadmin:
    image: phpmyadmin:latest
    container_name: pma
    environment:
      PMA_HOST: mysql
      PMA_PORT: 3306
    ports:
      - "8080:80"

  # 2. Backend (Spring Boot + Gradle)
  backend-service:
    build: ./TpDJFSpring
    container_name: backend-container
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://localhost:3307/mydb
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
    depends_on:
      - mysql-db

  # 3. Frontend (Python HTTP Server)
  frontend-service:
    build: ./Front
    container_name: frontend-container
    ports:
      - "8000:8000"
    depends_on:
      - backend-service
      
  # 4. Nginx (Proxy)
  nginx-proxy:
    image: nginx:stable
    container_name: nginx-proxy
    ports:
      - "80:80"
    volumes:
      - ./nginx-setup/default.conf:/etc/nginx/conf.d/default.conf:ro
    depends_on:
      - frontend-service
      - backend-service

volumes:
  db_data:
```

Ajouter le fichier `.env` : 
```
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=mydb
MYSQL_USER=root
MYSQL_PASSWORD=root
DB_PORT=3307
```



