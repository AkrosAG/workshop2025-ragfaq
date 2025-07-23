package ch.akros.workshop.ragfaq;

import javax.sql.DataSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RagfaqApplication {

	@Bean
	ChatClient ai(PromptChatMemoryAdvisor promptChatMemoryAdvisor,
		ChatClient.Builder aibuilder) {
		return aibuilder
			.defaultAdvisors(promptChatMemoryAdvisor)
			.build();
	}

	@Bean
	PromptChatMemoryAdvisor promptChatMemoryAdvisor(DataSource dataSource) {
		var jdbc = JdbcChatMemoryRepository
			.builder()
			.dataSource(dataSource)
			.build();

		var chatMessageWindow = MessageWindowChatMemory
			.builder()
			.chatMemoryRepository(jdbc)
			.build();

		return PromptChatMemoryAdvisor
			.builder(chatMessageWindow)
			.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(RagfaqApplication.class, args);
	}

}
