FROM gradle:8.5-jdk21-jammy AS build

WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .

# On construit le JAR
# Le flag --no-daemon est important pour Docker
RUN gradle bootJar -x test --no-daemon

# --- Étape 2 : Lancer l'appli (Version 21) ---
# On utilise l'image légère Temurin 21 pour l'exécution
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# On récupère le JAR généré à l'étape précédente
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
