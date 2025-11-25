// gateway/build.gradle.kts

plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.ssafy"
version = "0.0.1-SNAPSHOT"
description = "Gateway"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

// 1. Spring Boot 3.5.6과 호환되는 Spring Cloud 버전 변수 정의
extra["springCloudVersion"] = "2025.0.0"

// 2. Spring Cloud 의존성 버전 관리를 위한 dependencyManagement 추가
dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${project.extra["springCloudVersion"]}")
    }
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    
    // JWT 검증에 필요한 의존성
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    
    // Lombok, Configuration Processor
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Test 의존성
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")

    // .env 파일 로드
    implementation("me.paulschwarz:spring-dotenv:3.0.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}