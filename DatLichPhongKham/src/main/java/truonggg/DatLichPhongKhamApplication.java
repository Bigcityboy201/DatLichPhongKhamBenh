package truonggg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // Enable asynchronous processing for @Async annotations
public class DatLichPhongKhamApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatLichPhongKhamApplication.class, args);
	}
}
