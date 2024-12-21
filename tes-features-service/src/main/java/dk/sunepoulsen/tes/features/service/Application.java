package dk.sunepoulsen.tes.features.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Main application object of the backend service.
 * <p>
 *     It contains no special logic besides global annotations to work with <code>Spring Boot</code>
 * </p>
 */
@EnableWebMvc
@EnableAsync
@EnableWebSecurity
@SpringBootApplication( scanBasePackages = {
    "dk.sunepoulsen.tes"
})
public class Application {
    /**
     * Main function.
     * @param args No used, but passed to <code>SpringApplication.run()</code>
     */
    public static void main( String[] args ) {
        SpringApplication.run( Application.class, args );
    }
}
