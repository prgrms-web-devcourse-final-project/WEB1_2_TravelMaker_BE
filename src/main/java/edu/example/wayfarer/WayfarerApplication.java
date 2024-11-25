package edu.example.wayfarer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WayfarerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WayfarerApplication.class, args);
    }

}
