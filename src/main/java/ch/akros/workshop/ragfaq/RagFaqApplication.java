package ch.akros.workshop.ragfaq;

import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
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
public class RagFaqApplication {

    private static final Logger LOG = LoggerFactory.getLogger(RagFaqApplication.class);

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
        SpringApplication.run(RagFaqApplication.class, args);
    }

}
