package ch.akros.workshop.ragfaq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RagFaqApplication {
	private static final Logger LOG = LoggerFactory.getLogger(RagFaqApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(RagFaqApplication.class, args);
	}

}
