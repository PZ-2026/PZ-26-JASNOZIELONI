package pl.edu.ur.coopspace_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot entry point for the Coopspace backend application.
 * This is the start application class.
 */
@SpringBootApplication
public class CoopspaceBackendApplication {

	/**
	 * Creates the application bootstrap class.
	 */
	public CoopspaceBackendApplication() {
	}

	/**
	 * Starts the Spring Boot application.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(CoopspaceBackendApplication.class, args);
	}

}
