package ch.akros.workshop.ragfaq;

import org.springframework.boot.SpringApplication;

public class TestRagfaqApplication {

	public static void main(String[] args) {
		SpringApplication.from(RagfaqApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
