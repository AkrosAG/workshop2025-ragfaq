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
3. Lasst einmal den Test `RagfaqApplicationTests` laufen, um sicherzustellen, dass alles funktioniert.
4. Passt die `spring.ai.ollama.base-url` in der `application.properties` an, um auf euren Ollama Server zu zeigen.

## Implementierung

### Controller zur Interarktion mit der Anwendung

### Gedächtnis hinzufügen

### Verwendung eines System Prompts 
Mit dem System Prompt können wir dem LLM mitteilen, was sein Auftrag ist und wie es sich verhalten soll.

### Observability der Kosten

`http://localhost:8080/actuator/metrics/gen_ai.client.token.usage`

### Retrieval-Augmented Generation (R.A.G.) mit Vector Databank