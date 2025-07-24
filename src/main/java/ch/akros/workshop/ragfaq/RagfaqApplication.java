package ch.akros.workshop.ragfaq;

import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;

@SpringBootApplication
public class RagfaqApplication {

    private static final Logger LOG = LoggerFactory.getLogger(RagfaqApplication.class);

    @Bean
    ChatClient ai(PromptChatMemoryAdvisor promptChatMemoryAdvisor, VectorStore vectorStore,
        ChatClient.Builder aibuilder) {
        var system = """
            Du bist ein KI-gestützter Assistent, der Menschen hilft, die AcmeCloud verwenden wollen. Nachfolgend findest du die relevanten Teile der AcmeCloud 
            Dokumentation. Wenn du dort keine Informationen findest, dann antworte höflich, dass du leider nicht weiterhelfen kannst und sie könnten den Support
            unter +41 31 123 45 67 anrufen.""";

        return aibuilder
            .defaultSystem(system)
            .defaultAdvisors(promptChatMemoryAdvisor, new QuestionAnswerAdvisor(vectorStore))
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

    @Bean
    CommandLineRunner initializeVectorStore(JdbcClient db, VectorStore vectorStore) {
        return args -> {
            var count = db
                .sql("select count(*) from vector_store")
                .query(Integer.class)
                .single();
            if (count == 0) {
                LOG.info("No vectors found, initializing vector store...");
                List<Document> documents = MarkdownLoader.loadMarkdownFiles("docs");
                vectorStore.add(documents);
                LOG.info("Successfully initialized vector store with {} vectors.", documents.size());

            } else {
                LOG.info("Vector store already initialized with {} vectors.", count);
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(RagfaqApplication.class, args);
    }

}
