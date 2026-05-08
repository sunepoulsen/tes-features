package dk.sunepoulsen.tes.features.service;

import org.springframework.boot.SpringApplication;

public class LocalDevApplication {

    static void main(String[] args) {
        SpringApplication
            .from(Application::main)
            .with(LocalContainersConfig.class)
            .run(args);
    }

}
