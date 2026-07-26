package ir.tahamohamadi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TahaMohamadiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TahaMohamadiBackendApplication.class, args);
    }
}
