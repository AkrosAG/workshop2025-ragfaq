package ch.akros.workshop.ragfaq;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RagfaqApplication {

	@Bean
	ChatClient ai(ChatClient.Builder aibuilder) {
		return aibuilder.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(RagfaqApplication.class, args);
	}

}
