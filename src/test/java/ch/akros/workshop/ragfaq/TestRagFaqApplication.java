package ch.akros.workshop.ragfaq;

import org.springframework.boot.SpringApplication;

public class TestRagFaqApplication {

	public static void main(String[] args) {
		SpringApplication.from(RagFaqApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
