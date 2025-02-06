# Используем JDK 17
FROM eclipse-temurin:17-jdk

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем Gradle файлы и код
COPY . .

# Собираем JAR
RUN ./gradlew clean bootJar

# Запускаем приложение
CMD ["java", "-jar", "build/libs/bot.jar"]