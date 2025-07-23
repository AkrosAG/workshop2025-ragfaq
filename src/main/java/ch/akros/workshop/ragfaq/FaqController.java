package ch.akros.workshop.ragfaq;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaqController {
    private final ChatClient ai;

    public FaqController(ChatClient ai) {
        this.ai = ai;
    }

    @GetMapping("/faq")
    String getAnswer(@RequestParam String question) {
        return ai
            .prompt()
            .user(question)
            .call()
            .content();
    }
}
