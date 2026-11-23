// Microservicio propio del equipo: Spring Boot + JPA (H2) + JWT + Swagger/OpenAPI.
// Guarda lo que la Lince API del cliente NO permite escribir: hitos (check-in/out),
// geocercas, incidencias, disponibilidad y confirmaciones de servicios.
plugins {
    java
    id("org.springframework.boot") version "4.1.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "cl.duoc.lince"
version = "1.0.0"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(25) }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-restclient")

    // Documentación Swagger UI en /swagger-ui.html
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1")

    // JWT (token de sesión con el rol del usuario)
    implementation("io.jsonwebtoken:jjwt-api:0.12.7")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.7")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.7")

    // Base de datos H2 guardada en archivo (./data/lince.mv.db)
    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
