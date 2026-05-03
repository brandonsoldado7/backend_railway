package neuro_activity.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NeuroActivityApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeuroActivityApplication.class, args);
	}
}
