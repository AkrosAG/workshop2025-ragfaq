# Hands-on Spring AI Workshop

## Ziel
In diesem Workshop werden wir eine Spring Boot Anwendung entwickeln, die Spring AI nutzt, um ein RAG (Retrieval-Augmented Generation) System zu erstellen, um Fragen zu beantworten. 
Wir werden dabei die Ollama verwenden, um ein LLM (Large Language Model) zu integrieren.
Im Verzeichnis `src/main/resources/docs` findet ihr die Dokumentation des fiktiven Unternehmens **AcmeCloud**, die wir als Wissensbasis verwenden werden.

Diese Dokumentation wird in eine Vektor-Datenbank geladen, um sie später für die Beantwortung von Fragen zu nutzen.
Ausserdem werden wir unserem Assistent ein Gedächtnis geben, damit er sich an frühere Fragen und Antworten erinnern kann.

## Vor dem Start
1. Stellt sicher, dass ihr Docker installiert habt.
2. Die Datei `compose.yml` startet eine PostgreSQL Datenbank mit der pgvector Extension.
Ihr könnt die Datenbank mit dem folgenden Befehl starten 
```bash
docker compose up -d
``` 
3. Lasst einmal den Test `RagFaqApplicationTests` laufen, um sicherzustellen, dass alles funktioniert.
4. Passt die `spring.ai.ollama.base-url` in der `application.properties` an, um auf euren Ollama Server zu zeigen.

## Implementierung
Wir werden nun Schritt für Schritt die Implementierung des RAG Systems vornehmen.
Eine fertige implementierung ist im Branch `feature/complete-implementation` verfügbar.

### Interarktion mit dem LLM
Um mit dem LLM und mit unserer Anwendung interagieren zu können erstellen wir einen Controller `RagFaqController`:

```java
@RestController
public class RagFaqController {
    private final ChatClient ai;

    public RagFaqController(ChatClient.Builder aiBuilder) {
        this.ai = aiBuilder.build();
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
```

Im Konstruktor wird ein `ChatClient.Builder` injeziert, der es uns erlaubt einen `ChatClient` zu konfigurieren. 
Mit diesem können wir in der Methode `getAnswer` mit dem LLM interagieren.

Nun könnt ihr mit dem HTTP Client (z.B. `curl` oder den Browser) eurer Wahl einen Request absetzen:

> **Vorsicht:** In PowerShell gibt es ein alias `curl` auf `Invoke-WebRequest`.
> Um diesen zu entfernen, könnt ihr `Remove-Item alias:curl` verwenden.

```bash
curl -G --data-urlencode "question=Hi ich bin Kevin. Wer bist du?" http://localhost:8080/faq
```

Super, nun können wir mit unserem neuen Freund sprechen.

### Gedächtnis hinzufügen
Leider ist dieser sehr vergesslich und kann sich bei der nächsten Frage nicht mehr an uns erinnern:

```bash
curl -G --data-urlencode "question=Hi wie ist mein Name?" http://localhost:8080/faq
```

Geben wir unserem Freun ein Gedächtnis.
Dazu verwenden wir einen _Advisor_, der ein Post- und Pre-Processing bei unseren Anfragen durchführt.
Wir fügen in der Klasse `RagFaqApplication` einen `PromptChatMemoryAdvisor` hinzu:

```java
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
```

Dieser Advisor wird für uns die Nachrichten innerhalb unserer Unterhaltungen persistieren und sie beim nächsten Aufruf dem LLM mitgeben.

Dafür müssen wir die Konfiguration unseres `ChatClient` in der Klasse `RagFaqController` anpassen:

```java
    // ...
    public RagFaqController(PromptChatMemoryAdvisor promptChatMemoryAdvisor,  // new
                            ChatClient.Builder aiBuilder) {
        this.ai = aiBuilder
            .defaultAdvisors(promptChatMemoryAdvisor) // new
            .build();
    }
    // ...
```

Nun müssen wir noch die Methode `getAnswer` anpassen, damit wir eine `conversationId` mitgeben können:

```java
    // ...
    @GetMapping("/{conversation}/faq") // changed
        String getAnswer(@PathVariable String conversation, // new
                         @RequestParam String question) {
            return ai
                .prompt()
                .user(question)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversation)) // new
                .call()
                .content();
    }
    // ...
```

Versuchen wir es erneut:
```bash
curl -G --data-urlencode "question=Hi ich bin Kevin. Wer bist du?" http://localhost:8080/chat1/faq
curl -G --data-urlencode "question=Hi wie ist mein Name?" http://localhost:8080/chat1/faq 
```

Super, unsere Beziehung zu einanden ist gefestigt.
In der Datenbank sind die Daten in der Tabelle `spring_ai_chat_memory` zu sehen:

| conversation\_id | content | type | timestamp |
| :--- | :--- | :--- | :--- |
| chat1 | Hi ich bin Kevin. Wer bist du? | USER | 2025-07-25 08:04:40.479000 |
| chat1 | Hallo Kevin! Ich bin eine KI, eine Art Computerprogramm, das darauf trainiert ist, mit Menschen zu kommunizieren und Fragen beantworten zu können. Was kann ich für dich tun? Hast du ein bestimmtes Thema auf dem Herzen oder möchtest du einfach nur ein bisschen plaudern? | ASSISTANT | 2025-07-25 08:04:40.480000 |
| chat1 | Hi wie ist mein Name? | USER | 2025-07-25 08:04:40.481000 |
| chat1 | Dein Name ist Kevin! Wir hatten vorhin schon ein Gespräch, hast du dich noch daran erinnert? | ASSISTANT | 2025-07-25 08:04:40.482000 |


### Verwendung eines System Prompts 
Nun müssen wir unserem Freund mitteilen was seine Aufgabe ist.
Dazu passen wir erneut die Konfiguration unserer `ChatClient` an und verwenden einen System Prompt:

```java
    // ...
    public RagFaqController(PromptChatMemoryAdvisor promptChatMemoryAdvisor, ChatClient.Builder aiBuilder) {
        var system = """
            Du bist ein KI-gestützter Assistent, der Menschen hilft, die AcmeCloud verwenden wollen. Nachfolgend findest du die relevanten Teile der AcmeCloud 
            Dokumentation. Wenn du dort keine Informationen findest, dann antworte höflich, dass du leider nicht weiterhelfen kannst und sie könnten den Support
            unter +41 31 123 45 67 anrufen.""";

        this.ai = aiBuilder
            .defaultSystem(system) // new
            .defaultAdvisors(promptChatMemoryAdvisor)
            .build();
    }
    // ...
```

Wiederholen wir nun unserer anfangs gestellte Frage, sieht seine Antwort anders aus:

```bash
curl -G --data-urlencode "question=Hi ich bin Kevin. Wer bist Du?" http://localhost:8080/chat3/faq
```

Antwort:
```text
Hallo Kevin! Ich bin ein KI-gestützter Assistent, der dir bei Fragen zu AcmeCloud helfen möchte. Wie kann ich dir heute helfen? Möchtest du etwas über unsere Cloud-Dienste erfahren oder hast du bereits eine spezielle Frage?
```

### Retrieval-Augmented Generation (R.A.G.) mit Vector Databank
Nun ist es an der Zeit das Wissen des Assistenten zu erweitern. 
Stellen wir ihm die Dokumentation der AcmeCloud zur Verfügung.

Dazu müssen wir zunächst die Dokumentation im Verzeichnis `src/main/resources/docs` in eine Vektordatenbank schreiben.
Aus den Dateien müssen wir Embeddings generieren. 
Die Konfiguration hierfür ist in der `application.properties` Datei bereits gemacht. 
Zum Einlesen der Dateien gibt es bereits die Utility Klasse `MarkdownLoader`.

Um die Vektordatenbank initial beim Anwendungsstart zu befüllen, erweitern wir die Klasse `RagFaqApplication`:

```java
    // ...
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
    // ...
```

Damit der `ChatClient` vorher Daten aus der Vektordatenbank holt und diese im Kontextfenster mitgibt, müssen wir einen weiteren Advisor in der Klasse `RagFaqController` hinzufügen:

```java
    // ...
    public RagFaqController(PromptChatMemoryAdvisor promptChatMemoryAdvisor, 
                            VectorStore vectorStore, // new
                            ChatClient.Builder aiBuilder) {
        // ...
        this.ai = aiBuilder
            .defaultSystem(system)
            .defaultAdvisors(promptChatMemoryAdvisor,
                new QuestionAnswerAdvisor(vectorStore)) // new
            .build();
    }
    // ...
```

Nach einem Neustart sehen wir in den Logs, dass die Vektordatenbank initialisiert wurde:

```text
2025-07-25T08:40:54.569+02:00  INFO 13460 --- [ragfaq] [  restartedMain] c.a.workshop.ragfaq.RagFaqApplication    : No vectors found, initializing vector store...
2025-07-25T08:40:55.143+02:00  INFO 13460 --- [ragfaq] [  restartedMain] c.a.workshop.ragfaq.RagFaqApplication    : Successfully initialized vector store with 3 vectors.
```

Die Daten der Vektordatenbank sind in der Tabelle `vector_store` gepseichert.

Testen wir das Ganze und fragen wie wir am besten bei AcmeCloud anfangen sollten:

```bash
curl -G --data-urlencode "question=Hi ich bin Kevin. Ich starte gerade mit AcmeCloud. Was sind die ersten Schritte um AcmeCloud verwenden zu können?" http://localhost:8080/chat4/faq
```

Super, erhat die Dokumentation aus der Datei `getting-started.md` gefunden und uns die notwendigen Informationen zur Verfügung gestellt.

Bei den Antworten kommt es stark auf die Embeddings in der Datenbank an. 
Die Klasse `MarkdownLoader` enthält auch die Methode `loadMarkdownFilesByLine`.
Verwendet ihr diese wird jede Zeile einzeln in die Vektordatenbank geschrieben.

Um die Daten in der Vektordatenbank zu löschen, ist es am einfachsten den Docker Container neuzustarten:

```bash
docker compose down
docker compose up -d
```

### Kosten im Blick behalten
Gerade bei den öffentlichen Modellen von Anthropic oder OpenAi verursacht jede Interaktion mit einem Modell Token-Kosten.
Auch hier bietet Spring AI, durch die Integration mit dem Actuator Modul, die Möglichkeit diese Kosten im Blick zu haben.
Dazu fügen wir die folgenden Zeilen zur Datei `application.properties` hinzu:

```properties
# Actuator configuration
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
```

Nach einem Neustart der Anwendung und einer erneuten Anfrage an unserem Assistenten, können wir uns die verfügbaren Metriken im Browser anschauen:

- http://localhost:8080/actuator/metrics
- http://localhost:8080/actuator/metrics/gen_ai.client.token.usage
