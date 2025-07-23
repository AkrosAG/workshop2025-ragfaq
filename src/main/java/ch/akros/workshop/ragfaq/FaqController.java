package ch.akros.workshop.ragfaq;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaqController {
    private final ChatClient ai;

    public FaqController(ChatClient ai) {
        this.ai = ai;
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
