package ch.akros.workshop.ragfaq;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RagFaqController {
    private final ChatClient ai;

    public RagFaqController(PromptChatMemoryAdvisor promptChatMemoryAdvisor, VectorStore vectorStore, ChatClient.Builder aiBuilder) {
        var system = """
            Du bist ein KI-gestützter Assistent, der Menschen hilft, die AcmeCloud verwenden wollen. Nachfolgend findest du die relevanten Teile der AcmeCloud 
            Dokumentation. Wenn du dort keine Informationen findest, dann antworte höflich, dass du leider nicht weiterhelfen kannst und sie könnten den Support
            unter +41 31 123 45 67 anrufen.""";

        this.ai = aiBuilder
            .defaultSystem(system)
            .defaultAdvisors(promptChatMemoryAdvisor, new QuestionAnswerAdvisor(vectorStore))
            .build();
    }

    @GetMapping("/{conversation}/faq")
    String getAnswer(@PathVariable String conversation, @RequestParam String question) {
        return ai
            .prompt()
            .user(question)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversation))
            .call()
            .content();
    }
}
