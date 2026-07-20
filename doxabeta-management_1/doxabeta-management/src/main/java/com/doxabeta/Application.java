package com.doxabeta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Doxabeta Management System backend.
 *
 * This is a standard Spring Boot application. {@code @SpringBootApplication} is a
 * shorthand for three annotations:
 *   - @Configuration     -> this class can define Spring beans
 *   - @EnableAutoConfiguration -> Spring Boot wires up sensible defaults (embedded
 *                          Tomcat, Jackson JSON, JPA/Hibernate, etc.) based on what's
 *                          on the classpath (see pom.xml dependencies)
 *   - @ComponentScan     -> Spring scans this package and all sub-packages
 *                          (com.doxabeta.*) for @Component/@Service/@Repository/
 *                          @Controller/@Configuration classes and registers them
 *
 * On startup, Spring will also automatically run any bean that implements
 * {@link org.springframework.boot.CommandLineRunner}. That's how
 * {@link com.doxabeta.service.SeedService} loads students.csv into the database
 * every time the app starts (see that class for details).
 */
@SpringBootApplication
public class Application {

    /**
     * Standard Java main method. Delegates to Spring Boot, which:
     *   1. Creates the Spring ApplicationContext (the container that holds all beans)
     *   2. Starts the embedded web server (Tomcat, on port 8080 by default)
     *   3. Runs any CommandLineRunner beans (this is where CSV seeding happens)
     *
     * @param args command-line arguments, e.g. --spring.profiles.active=postgres
     *             (see README.md for how profiles switch between H2 and PostgreSQL)
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
